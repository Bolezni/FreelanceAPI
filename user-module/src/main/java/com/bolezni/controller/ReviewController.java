package com.bolezni.controller;

import com.bolezni.dto.ApiResponse;
import com.bolezni.dto.ReviewCreateDto;
import com.bolezni.dto.ReviewResponseDto;
import com.bolezni.dto.ReviewUpdateDto;
import com.bolezni.service.ReviewedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final ReviewedService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> getReviewById(@PathVariable(name = "id") Long id) {
        ReviewResponseDto dto = reviewService.getReview(id);
        ApiResponse<ReviewResponseDto> apiResponse = ApiResponse.<ReviewResponseDto>builder()
                .status(true)
                .data(dto)
                .message("The request was successful")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(@RequestBody @Valid ReviewCreateDto reviewCreateDto) {
        ReviewResponseDto dto = reviewService.createReview(reviewCreateDto);

        ApiResponse<ReviewResponseDto> apiResponse = ApiResponse.<ReviewResponseDto>builder()
                .status(true)
                .data(dto)
                .message("Successful create review")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(@PathVariable(name = "id") Long id,
                                                                       @RequestBody @Valid ReviewUpdateDto reviewUpdateDto) {
        ReviewResponseDto dto = reviewService.updateReview(id, reviewUpdateDto);

        ApiResponse<ReviewResponseDto> apiResponse = ApiResponse.<ReviewResponseDto>builder()
                .status(true)
                .data(dto)
                .message("Successful update review")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/user/all")
    public ResponseEntity<Page<ReviewResponseDto>> getAllReviewsCurrentUser(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> reviewsPage = reviewService.getAllReviewsCurrentUser(pageable);
        return ResponseEntity.ok(reviewsPage);
    }

    @GetMapping("/user/{userId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getUserReviews(
            @PathVariable(name = "userId") String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewResponseDto> reviews = reviewService.getAllReviewsByReviewedUser(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<ReviewResponseDto> updateReviewStatus(
            @PathVariable(name = "id") Long id,
            @PathVariable(name = "status") String status) {

        ReviewResponseDto updatedReview = reviewService.updateReviewStatus(id, status);
        return ResponseEntity.ok(updatedReview);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable(name = "id") Long id) {
        reviewService.deleteReview(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(true)
                .message("Successful delete review")
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
