package sn.bmbacke.rh.payload.mapper;


import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.PositionDTO;
import sn.bmbacke.rh.entity.Position;


@Mapper(componentModel = "spring")
public interface PositionMapper {

    PositionDTO toDto(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    Position toEntity(PositionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    void updateEntityFromDto(PositionDTO dto, @MappingTarget Position position);
}
