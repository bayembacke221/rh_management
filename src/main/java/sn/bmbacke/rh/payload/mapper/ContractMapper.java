package sn.bmbacke.rh.payload.mapper;

import org.mapstruct.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.Employee;


@Mapper(componentModel = "spring",
        uses = {EmployeeMapper.class, DocumentMapper.class})
public interface ContractMapper {

    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "documents", source = "documents", qualifiedByName = "toDocumentShort")
    ContractDTO toDto(Contract contract);

    @Named("toContractShort")
    ContractShortDTO toShortDto(Contract contract);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", source = "employeeId", qualifiedByName = "contractMapperEmployeeFromId")
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "terminationReason", ignore = true)
    Contract toEntity(ContractCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "terminationReason", ignore = true)
    void updateEntityFromDto(ContractUpdateDTO dto, @MappingTarget Contract contract);

    @Named("contractMapperEmployeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }
}