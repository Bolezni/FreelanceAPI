package com.bolezni.mapper;

import com.bolezni.dto.ReviewResponseDto;
import com.bolezni.model.ReviewEntity;
import com.bolezni.model.ReviewStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "status", qualifiedByName = "mapStatusToString")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewedUserId", source = "reviewedUser.id")
    ReviewResponseDto mapToDto(ReviewEntity entity);

    @Named("mapStatusToString")
    default String mapStatusToString(ReviewStatus status) {
        return status.name();
    }
}
