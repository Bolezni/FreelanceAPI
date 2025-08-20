package com.bolezni.service;

import com.bolezni.dto.ReviewCreateDto;
import com.bolezni.dto.ReviewResponseDto;
import com.bolezni.dto.ReviewUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewedService {
    ReviewResponseDto createReview(ReviewCreateDto reviewCreateDto);

    ReviewResponseDto getReview(Long id);

    ReviewResponseDto updateReview(Long id, ReviewUpdateDto reviewUpdateDto);

    Page<ReviewResponseDto> getAllReviewsCurrentUser(Pageable pageable);

    Page<ReviewResponseDto> getAllReviewsByReviewedUser(String reviewedId, Pageable pageable);
}
