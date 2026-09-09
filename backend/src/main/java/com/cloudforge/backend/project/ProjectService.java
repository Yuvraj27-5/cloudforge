package com.cloudforge.backend.project;

import com.cloudforge.backend.common.exception.DuplicateResourceException;
import com.cloudforge.backend.common.exception.ResourceNotFoundException;
import com.cloudforge.backend.project.dto.CreateProjectRequest;
import com.cloudforge.backend.project.dto.ProjectResponse;
import com.cloudforge.backend.project.dto.UpdateProjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private static final String RESOURCE = "Project";

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public Page<ProjectResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(ProjectResponse::from);
    }

    public ProjectResponse findById(UUID id) {
        return ProjectResponse.from(getOrThrow(id));
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        if (repository.existsByName(request.name())) {
            throw new DuplicateResourceException(RESOURCE, "name", request.name());
        }

        Project project = new Project(
                request.name(),
                request.repositoryUrl(),
                request.defaultBranchOrMain(),
                request.cloudProvider(),
                request.environment(),
                request.description()
        );

        Project saved = repository.save(project);
        log.info("Created project id={} name={}", saved.getId(), saved.getName());
        return ProjectResponse.from(saved);
    }

    @Transactional
    public ProjectResponse update(UUID id, UpdateProjectRequest request) {
        Project project = getOrThrow(id);

        if (repository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException(RESOURCE, "name", request.name());
        }

        // No repository.save(): the entity is managed inside this transaction,
        // so the change is flushed on commit.
        project.update(
                request.name(),
                request.repositoryUrl(),
                request.defaultBranch(),
                request.cloudProvider(),
                request.environment(),
                request.description()
        );

        log.info("Updated project id={}", id);
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = getOrThrow(id);
        repository.delete(project);
        log.info("Deleted project id={}", id);
    }

    private Project getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }
}
