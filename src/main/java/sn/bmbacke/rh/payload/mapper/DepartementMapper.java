package sn.bmbacke.rh.payload.mapper;



import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.entity.Employee;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {EmployeeMapper.class})
public interface DepartementMapper {

    @Mapping(target = "manager", source = "manager", qualifiedByName = "toEmployeeShort")
    @Mapping(target = "parentDepartement", source = "parentDepartement", qualifiedByName = "toDepartementShort")
    DepartementDTO toDto(Departement departement);

    @Named("toDepartementShort")
    DepartementShortDTO toShortDto(Departement departement);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "employeeFromId")
    @Mapping(target = "parentDepartement", source = "parentDepartementId", qualifiedByName = "departementFromId")
    Departement toEntity(DepartementCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "manager", source = "managerId", qualifiedByName = "employeeFromId")
    @Mapping(target = "parentDepartement", source = "parentDepartementId", qualifiedByName = "departementFromId")
    void updateEntityFromDto(DepartementUpdateDTO dto, @MappingTarget Departement departement);

    @Named("departementFromId")
    default Departement departementFromId(Long id) {
        if (id == null) {
            return null;
        }
        Departement departement = new Departement();
        departement.setId(id);
        return departement;
    }

    @Named("employeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }

    /**
     * Convertit une liste de départements en structure arborescente
     */
    default List<DepartementTreeDTO> toDepartementTree(List<Departement> departements) {
        if (departements == null) {
            return null;
        }

        // Récupérer tous les départements parents (ceux qui n'ont pas de parent)
        return departements.stream()
                .filter(dept -> dept.getParentDepartement() == null)
                .map(dept -> buildDepartementTree(dept, departements))
                .toList();
    }

    /**
     * Méthode auxiliaire pour construire l'arbre de départements
     */
    default DepartementTreeDTO buildDepartementTree(Departement dept, List<Departement> allDepts) {
        DepartementTreeDTO treeDTO = new DepartementTreeDTO();
        treeDTO.setId(dept.getId());
        treeDTO.setName(dept.getName());
        treeDTO.setCode(dept.getCode());
        treeDTO.setActive(dept.getActive());

        if (dept.getManager() != null) {
            EmployeeShortDTO managerDTO = new EmployeeShortDTO();
            managerDTO.setId(dept.getManager().getId());
            managerDTO.setFirstName(dept.getManager().getFirstName());
            managerDTO.setLastName(dept.getManager().getLastName());
            treeDTO.setManager(managerDTO);
        }

        // Récupérer tous les départements enfants
        List<DepartementTreeDTO> children = allDepts.stream()
                .filter(d -> d.getParentDepartement() != null && d.getParentDepartement().getId().equals(dept.getId()))
                .map(child -> buildDepartementTree(child, allDepts))
                .toList();

        treeDTO.setChildren(children);
        return treeDTO;
    }
}