package sn.bmbacke.rh.payload.mapper;


import org.mapstruct.*;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.payload.dto.PositionCreateDTO;
import sn.bmbacke.rh.payload.dto.PositionDTO;
import sn.bmbacke.rh.entity.Position;
import sn.bmbacke.rh.payload.dto.PositionShortDTO;
import sn.bmbacke.rh.payload.dto.PositionUpdateDTO;


@Mapper(componentModel = "spring", uses = {DepartementMapper.class})
public interface PositionMapper {

    @Mapping(target = "department", source = "department")
    PositionDTO toDto(Position position);

    @Named("toPositionShort")
    PositionShortDTO toShortDto(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "department", source = "departmentId", qualifiedByName = "positionMapperDepartementFromId")
    Position toEntity(PositionCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "department", source = "departmentId", qualifiedByName = "positionMapperDepartementFromId")
    void updateEntityFromDto(PositionUpdateDTO dto, @MappingTarget Position position);

    @Named("positionMapperDepartementFromId")
    default Departement departementFromId(Long id) {
        if (id == null) {
            return null;
        }
        Departement departement = new Departement();
        departement.setId(id);
        return departement;
    }
}