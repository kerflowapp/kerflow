package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.prospects.domain.ProspectPipelineColumnDto;
import com.kerflowapp.kerflow.domain.ProspectPipelineColumn;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProspectPipelineColumnMapper {

    ProspectPipelineColumnDto toDto(ProspectPipelineColumn column);

    List<ProspectPipelineColumnDto> toDtoList(List<ProspectPipelineColumn> columns);

}

