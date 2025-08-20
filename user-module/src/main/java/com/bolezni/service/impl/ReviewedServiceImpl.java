package com.bolezni.service.impl;

import com.bolezni.dto.ReviewCreateDto;
import com.bolezni.dto.ReviewResponseDto;
import com.bolezni.dto.ReviewUpdateDto;
import com.bolezni.mapper.ReviewMapper;
import com.bolezni.model.ReviewEntity;
import com.bolezni.model.ReviewStatus;
import com.bolezni.model.UserEntity;
import com.bolezni.repository.ReviewerRepository;
import com.bolezni.repository.UserRepository;
import com.bolezni.service.ReviewedService;
import com.bolezni.utils.UpdateFieldUtils;
import com.bolezni.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewedServiceImpl implements ReviewedService {
    private final ReviewerRepository reviewerRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewCreateDto reviewCreateDto) {
        if (reviewCreateDto == null) {
            log.error("reviewCreateDto is null");
            throw new IllegalArgumentException("reviewCreateDto is null");
        }

        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Unauthorized"));

        if (reviewerRepository.existsByReviewedUserIdAndReviewerId(reviewCreateDto.reviewedId(), currentUser.getId())) {
            log.error("Review already exists");
            throw new RuntimeException("Review already exists");
        }

        UserEntity reviewedUser = userRepository.findById(reviewCreateDto.reviewedId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ReviewEntity reviewEntity = ReviewEntity.builder()
                .reviewer(currentUser)
                .reviewedUser(reviewedUser)
                .rating(reviewCreateDto.rating())
                .status(ReviewStatus.valueOf(reviewCreateDto.status()))
                .comment(reviewCreateDto.comment())
                .build();

        reviewerRepository.save(reviewEntity);

        return reviewMapper.mapToDto(reviewEntity);
    }

    @Override
    public ReviewResponseDto getReview(Long id) {
        ReviewEntity review = reviewerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        return reviewMapper.mapToDto(review);
    }

    @Override
    @Transactional
    public ReviewResponseDto updateReview(Long id, ReviewUpdateDto reviewUpdateDto) {
        if (reviewUpdateDto == null) {
            log.error("reviewUpdateDto is null");
            throw new IllegalArgumentException("reviewUpdateDto is null");
        }

        ReviewEntity review = reviewerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Unauthorized or not found"));

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            log.error("Reviewer not match");
            throw new RuntimeException("Reviewer not match");
        }

        boolean hasChanged = updateFields(review, reviewUpdateDto);

        if (hasChanged) {
            ReviewEntity savedReview = reviewerRepository.save(review);
            return reviewMapper.mapToDto(savedReview);
        }

        return reviewMapper.mapToDto(review);
    }

    private boolean updateFields(ReviewEntity reviewEntity, ReviewUpdateDto reviewUpdateDto) {
        return UpdateFieldUtils.updateMultipleFields(
                () -> UpdateFieldUtils.updateField(reviewEntity.getComment(), reviewUpdateDto.comment(), reviewEntity::setComment),
                () -> UpdateFieldUtils.updateField(reviewEntity.getRating(), reviewUpdateDto.rating(), reviewEntity::setRating)
        );
    }

    @Override
    public Page<ReviewResponseDto> getAllReviewsCurrentUser(Pageable pageable) {
        UserEntity currentUser = UserUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Unauthorized or not found"));

        Page<ReviewEntity> reviews = reviewerRepository.findAllByReviewerId(pageable, currentUser.getId());

        return reviews.map(reviewMapper::mapToDto);
    }

    @Override
    public Page<ReviewResponseDto> getAllReviewsByReviewedUser(String reviewedId, Pageable pageable) {
        UserEntity reviewed = userRepository.findById(reviewedId)
                .orElseThrow(() -> new RuntimeException("Reviewed user not found"));

        Page<ReviewEntity> reviewPage = reviewerRepository.findAllByReviewedUserId(pageable, reviewed.getId());

        return reviewPage.map(reviewMapper::mapToDto);
    }
}
