package com.cloudforge.backend.risk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

/** Calls the FastAPI risk service. */
@Component
public class MlServiceRiskProvider implements RiskAssessmentProvider {

    private static final Logger log = LoggerFactory.getLogger(MlServiceRiskProvider.class);

    private final RestClient client;

    public MlServiceRiskProvider(
            @Value("${cloudforge.risk.ml-service-url:http://localhost:8000}") String baseUrl,
            @Value("${cloudforge.risk.timeout-seconds:5}") int timeoutSeconds) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        // A short timeout on purpose. A hanging risk engine must not hold a pipeline
        // open indefinitely; failing fast lets the decision engine apply its
        // fail-closed policy while the deployment is still waiting.
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public RiskScore score(RiskFeatures features) {
        try {
            MlRiskResponse response = client.post()
                    .uri("/predict-risk")
                    // Propagates the deployment's correlation ID into the ML service logs.
                    .header("X-Correlation-Id", MDC.get("correlationId"))
                    .body(MlRiskRequest.from(features))
                    .retrieve()
                    .body(MlRiskResponse.class);

            if (response == null) {
                throw new RiskProviderUnavailableException("Risk service returned an empty body", null);
            }

            return response.toRiskScore();

        } catch (RestClientException ex) {
            // Not caught and defaulted. The caller decides what an unavailable risk
            // engine means; this class must not quietly turn it into "low risk".
            log.error("Risk service call failed: {}", ex.getMessage());
            throw new RiskProviderUnavailableException("Risk service unavailable", ex);
        }
    }

    @Override
    public String name() {
        return "ml-service";
    }

    record MlRiskRequest(
            int files_changed,
            int lines_added,
            int lines_deleted,
            double test_pass_rate,
            double test_coverage,
            int code_complexity,
            int critical_vulnerabilities,
            int high_vulnerabilities,
            int previous_deployment_failures
    ) {
        static MlRiskRequest from(RiskFeatures f) {
            return new MlRiskRequest(
                    f.filesChanged(), f.linesAdded(), f.linesDeleted(),
                    f.testPassRate(), f.testCoverage(), f.codeComplexity(),
                    f.criticalVulnerabilities(), f.highVulnerabilities(),
                    f.previousDeploymentFailures());
        }
    }

    record MlRiskResponse(
            int risk_score,
            String risk_level,
            String decision,
            double confidence,
            List<MlFactor> factors,
            int baseline_score,
            String model_version,
            String trained_on
    ) {
        RiskScore toRiskScore() {
            // The service's own `decision` field is deliberately ignored. It is a
            // recommendation; the policy that turns a score into an action belongs
            // to CloudForge and must be changeable without redeploying the model.
            return new RiskScore(
                    risk_score,
                    RiskLevel.valueOf(risk_level),
                    confidence,
                    factors == null ? List.of() : factors.stream()
                            .map(f -> new RiskScore.Factor(
                                    f.feature(), f.value(), f.contribution(), f.explanation()))
                            .toList(),
                    model_version,
                    trained_on);
        }
    }

    record MlFactor(String feature, double value, double contribution, String explanation) {
    }
}
