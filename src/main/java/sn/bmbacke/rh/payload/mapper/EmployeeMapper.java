package sn.bmbacke.rh.payload.mapper;


import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Position;

@Mapper(componentModel = "spring",
        uses = {DepartementMapper.class, PositionMapper.class})
public interface EmployeeMapper {

    @Mapping(target = "departement", source = "departement")
    @Mapping(target = "position", source = "position")
    @Mapping(target = "manager", source = "manager", qualifiedByName = "toEmployeeShort")
    EmployeeDTO toDto(Employee employee);

    @Named("toEmployeeShort")
    EmployeeShortDTO toShortDto(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "departement", source = "departementId", qualifiedByName = "departementFromId")
    @Mapping(target = "position", source = "positionId", qualifiedByName = "positionFromId")
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "employeeFromId")
    @Mapping(target = "user", ignore = true)
    Employee toEntity(EmployeeCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cv", ignore = true)
    @Mapping(target = "departement", source = "departementId", qualifiedByName = "departementFromId")
    @Mapping(target = "position", source = "positionId", qualifiedByName = "positionFromId")
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "employeeFromId")
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(EmployeeUpdateDTO dto, @MappingTarget Employee employee);

    @Named("employeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }

    @Named("departementFromId")
    default Departement departementFromId(Long id) {
        if (id == null) {
            return null;
        }
        Departement departement = new Departement();
        departement.setId(id);
        return departement;
    }

    @Named("positionFromId")
    default Position positionFromId(Long id) {
        if (id == null) {
            return null;
        }
        Position position = new Position();
        position.setId(id);
        return position;
    }
}
