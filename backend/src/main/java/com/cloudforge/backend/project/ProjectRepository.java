package com.cloudforge.backend.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByName(String name);

    /** Used on update: renaming a project must not collide with itself. */
    boolean existsByNameAndIdNot(String name, UUID id);
}
