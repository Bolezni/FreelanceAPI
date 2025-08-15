package com.bolezni.repository;

import com.bolezni.model.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByAuthorId(String author_id);

    boolean existsByAuthorId(String author_id);

    Page<ProjectEntity> findAll(Pageable pageable);

    Page<ProjectEntity> findAllByAuthorId(Pageable pageable, String author_id);
}
