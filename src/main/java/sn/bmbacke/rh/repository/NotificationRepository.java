package sn.bmbacke.rh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.bmbacke.rh.entity.Notification;
import sn.bmbacke.rh.entity.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends GenericRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.id = :employeeId AND n.active = true AND n.expirationDate > :now ORDER BY n.createdDate DESC")
    Page<Notification> findActiveNotificationsForEmployee(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.systemWide = true AND n.active = true AND n.expirationDate > :now ORDER BY n.createdDate DESC")
    Page<Notification> findActiveSystemWideNotifications(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.id = :employeeId AND n.active = true AND n.expirationDate > :now AND n NOT IN (SELECT n2 FROM Notification n2 JOIN n2.readBy rb WHERE rb.id = :employeeId) ORDER BY n.createdDate DESC")
    Page<Notification> findUnreadNotificationsForEmployee(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n JOIN n.recipients r WHERE r.id = :employeeId AND n.active = true AND n.expirationDate > :now AND n NOT IN (SELECT n2 FROM Notification n2 JOIN n2.readBy rb WHERE rb.id = :employeeId)")
    Long countUnreadNotificationsForEmployee(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.id = :employeeId AND n.type = :type AND n.active = true AND n.expirationDate > :now ORDER BY n.createdDate DESC")
    Page<Notification> findNotificationsForEmployeeByType(@Param("employeeId") Long employeeId, @Param("type") NotificationType type, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT DISTINCT n.type FROM Notification n JOIN n.recipients r WHERE r.id = :employeeId AND n.active = true AND n.expirationDate > :now")
    List<NotificationType> findNotificationTypesForEmployee(@Param("employeeId") Long employeeId, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n WHERE n.sourceType = :sourceType AND n.sourceId = :sourceId ORDER BY n.createdDate DESC")
    List<Notification> findBySourceTypeAndSourceId(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);
}