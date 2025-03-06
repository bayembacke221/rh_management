package sn.bmbacke.rh.payload.mapper;



import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.entity.Employee;

import java.util.List;


@Mapper(componentModel = "spring")
public interface DepartementMapper {

    @Mapping(target = "manager", source = "manager")
    @Mapping(target = "parentDepartement", source = "parentDepartement", qualifiedByName = "toDepartementShort")
    DepartementDTO toDto(Departement departement);

    @Named("toDepartementShort")
    DepartementShortDTO toShortDto(Departement departement);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "manager", source = "managerId")
    @Mapping(target = "parentDepartement", source = "parentDepartementId")
    Departement toEntity(DepartementCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "manager", source = "managerId")
    @Mapping(target = "parentDepartement", source = "parentDepartementId")
    void updateEntityFromDto(DepartementUpdateDTO dto, @MappingTarget Departement departement);

    // Méthodes de conversion pour les mappings
    default Departement mapToDepartement(Long id) {
        if (id == null) {
            return null;
        }
        Departement departement = new Departement();
        departement.setId(id);
        return departement;
    }

    default Employee mapToEmployee(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }

    // Conversion de Employee vers EmployeeShortDTO
    default EmployeeShortDTO mapToEmployeeShortDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeShortDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .build();
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
            treeDTO.setManager(mapToEmployeeShortDTO(dept.getManager()));
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