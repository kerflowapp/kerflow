package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.prospects.domain.ProspectMessageDto;
import com.kerflowapp.kerflow.domain.ProspectMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProspectMessageMapper {

    ProspectMessageDto toDto(ProspectMessage message);

    List<ProspectMessageDto> toDtoList(List<ProspectMessage> messages);

}
