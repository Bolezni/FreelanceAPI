package com.bolezni.mapper;

import com.bolezni.dto.UserResponseDto;
import com.bolezni.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isVerified",source = "verified")
    UserResponseDto userToUserResponseDto(UserEntity user);

}
