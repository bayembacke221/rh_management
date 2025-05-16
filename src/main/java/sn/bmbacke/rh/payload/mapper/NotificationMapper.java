package sn.bmbacke.rh.payload.mapper;

import org.mapstruct.*;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Notification;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.payload.dto.NotificationDTO;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class})
public interface NotificationMapper {

    @Mapping(target = "recipients", source = "recipients", qualifiedByName = "toEmployeeShortList")
    @Mapping(target = "read", ignore = true)
    NotificationDTO toDto(Notification notification);

    @Named("toNotificationDtoWithReadStatus")
    @Mapping(target = "recipients", source = "recipients", qualifiedByName = "toEmployeeShortList")
    @Mapping(target = "read", expression = "java(notification.isRead(currentUser))")
    NotificationDTO toDtoWithReadStatus(Notification notification, @Context Employee currentUser);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "recipients", source = "recipientIds", qualifiedByName = "toEmployeeSet")
    @Mapping(target = "readBy", ignore = true)
    @Mapping(target = "active", constant = "true")
    Notification toEntity(NotificationCreateDTO dto);

    @Named("toEmployeeSet")
    default Set<Employee> toEmployeeSet(java.util.List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> {
                    Employee employee = new Employee();
                    employee.setId(id);
                    return employee;
                })
                .collect(Collectors.toSet());
    }
}