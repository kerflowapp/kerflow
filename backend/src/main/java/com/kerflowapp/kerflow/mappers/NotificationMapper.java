package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.authentication.domain.NotificationDTO;
import com.kerflowapp.kerflow.domain.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "isRead", source = "read")
    NotificationDTO toDto(Notification notification);

}
