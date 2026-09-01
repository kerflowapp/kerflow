package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.authentication.domain.UserDto;
import com.kerflowapp.kerflow.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = NotificationMapper.class)
public interface UserMapper {

    @Mapping(target = "subscription", ignore = true)
    UserDto toUserDto(User user);

}
