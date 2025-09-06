package com.bolezni.repository;

import com.bolezni.model.ProjectEntity;
import com.bolezni.model.ProjectStatus;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    boolean existsByAuthorId(String author_id);

    @NonNull
    Page<ProjectEntity> findAll(@NonNull Pageable pageable);

    Page<ProjectEntity> findAllByAuthorId(Pageable pageable, String author_id);

    long countByFreelancerIdAndStatus(@NonNull String freelancer_id, @NonNull ProjectStatus status);
}
