package com.cloudforge.backend.project;

import com.cloudforge.backend.common.exception.DuplicateResourceException;
import com.cloudforge.backend.common.exception.ResourceNotFoundException;
import com.cloudforge.backend.project.dto.CreateProjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectService service;

    private CreateProjectRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateProjectRequest(
                "payments-api",
                "https://github.com/acme/payments-api",
                null,
                CloudProvider.LOCAL_KUBERNETES,
                Environment.DEVELOPMENT,
                "Payment processing service"
        );
    }

    @Test
    void createDefaultsBranchToMainWhenOmitted() {
        when(repository.existsByName("payments-api")).thenReturn(false);
        when(repository.save(any(Project.class))).thenAnswer(call -> call.getArgument(0));

        var response = service.create(request);

        assertThat(response.defaultBranch()).isEqualTo("main");
        assertThat(response.name()).isEqualTo("payments-api");
        assertThat(response.id()).isNotNull();
    }

    @Test
    void createRejectsDuplicateName() {
        when(repository.existsByName("payments-api")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("payments-api");

        verify(repository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}
