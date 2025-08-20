package com.bolezni.repository;

import com.bolezni.model.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewerRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByReviewedUserIdAndReviewerId(String reviewedUser_id, String reviewer_id);

    Page<ReviewEntity> findAllByReviewerId(Pageable pageable, String reviewer_id);

    Page<ReviewEntity> findAllByReviewedUserId(Pageable pageable, String reviewedUser_id);
}
