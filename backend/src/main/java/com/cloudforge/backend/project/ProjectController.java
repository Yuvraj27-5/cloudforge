package com.cloudforge.backend.project;

import com.cloudforge.backend.common.dto.PageResponse;
import com.cloudforge.backend.project.dto.CreateProjectRequest;
import com.cloudforge.backend.project.dto.ProjectResponse;
import com.cloudforge.backend.project.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProjectResponse> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<ProjectResponse> page = service.findAll(pageable);
        return PageResponse.from(page, Function.identity());
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    /** @Valid is what triggers bean validation. Without it the DTO annotations do nothing. */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request,
            UriComponentsBuilder uriBuilder) {

        ProjectResponse created = service.create(request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/projects/{id}").build(created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateProjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
