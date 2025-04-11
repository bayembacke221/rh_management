package sn.bmbacke.rh.payload.mapper;

import org.mapstruct.*;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Leave;
import sn.bmbacke.rh.payload.dto.LeaveApprovalDTO;
import sn.bmbacke.rh.payload.dto.LeaveCreateDTO;
import sn.bmbacke.rh.payload.dto.LeaveDTO;
import sn.bmbacke.rh.payload.dto.LeaveUpdateDTO;

/**
 * Mapper pour les entités de type Leave
 */
@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface LeaveMapper {

    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "approvedBy", source = "approvedBy", qualifiedByName = "toEmployeeShort")
    LeaveDTO toDto(Leave leave);

    @Named("toLeaveShort")
    @Mapping(target = "employee", source = "employee", qualifiedByName = "toEmployeeShort")
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    LeaveDTO toShortDto(Leave leave);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", source = "employeeId", qualifiedByName = "leaveMapperEmployeeFromId")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    Leave toEntity(LeaveCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    void updateEntityFromDto(LeaveUpdateDTO dto, @MappingTarget Leave leave);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "leaveType", ignore = true)
    @Mapping(target = "reason", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvalDate", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    @Mapping(target = "halfDay", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateApprovalFromDto(LeaveApprovalDTO dto, @MappingTarget Leave leave);

    @Named("leaveMapperEmployeeFromId")
    default Employee employeeFromId(Long id) {
        if (id == null) {
            return null;
        }
        Employee employee = new Employee();
        employee.setId(id);
        return employee;
    }
}