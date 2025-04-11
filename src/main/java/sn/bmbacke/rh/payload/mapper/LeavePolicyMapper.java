package sn.bmbacke.rh.payload.mapper;
import org.mapstruct.*;
import sn.bmbacke.rh.entity.LeavePolicy;
import sn.bmbacke.rh.payload.dto.LeavePolicyCreateDTO;
import sn.bmbacke.rh.payload.dto.LeavePolicyDTO;

/**
 * Mapper pour les entités de type LeavePolicy
 */
@Mapper(componentModel = "spring")
public interface LeavePolicyMapper {

    LeavePolicyDTO toDto(LeavePolicy leavePolicy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    LeavePolicy toEntity(LeavePolicyCreateDTO dto);
}
