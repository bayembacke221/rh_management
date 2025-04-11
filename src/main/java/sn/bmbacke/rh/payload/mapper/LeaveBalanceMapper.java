package sn.bmbacke.rh.payload.mapper;
import org.mapstruct.*;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.LeaveBalance;
import sn.bmbacke.rh.payload.dto.LeaveBalanceCreateDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceDTO;

/**
 * Mapper pour les entités de type LeaveBalance
 */
@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface LeaveBalanceMapper {

    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "currentBalance", expression = "java(leaveBalance.getCurrentBalance())")
    LeaveBalanceDTO toDto(LeaveBalance leaveBalance);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", source = "employeeId", qualifiedByName = "leaveBalanceMapperEmployeeFromId")
    @Mapping(target = "usedBalance", constant = "0F")
    @Mapping(target = "adjustedBalance", ignore = true)
    @Mapping(target = "adjustmentReason", ignore = true)
    LeaveBalance toEntity(LeaveBalanceCreateDTO dto);

    @Named("leaveBalanceMapperEmployeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }
}