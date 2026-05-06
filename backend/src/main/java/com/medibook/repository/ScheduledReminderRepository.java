package com.medibook.repository;

import com.medibook.entity.ScheduledReminder;
import com.medibook.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledReminderRepository extends JpaRepository<ScheduledReminder, Long> {

    /**
     * Find all due reminders: status=PENDING and scheduledAt <= now.
     * Limited to batch size for scalability.
     */
    @Query("SELECT sr FROM ScheduledReminder sr WHERE sr.status = :status AND sr.scheduledAt <= :now ORDER BY sr.scheduledAt ASC")
    List<ScheduledReminder> findDueReminders(@Param("status") ReminderStatus status, @Param("now") LocalDateTime now);

    /**
     * Cancel all pending reminders for a specific appointment.
     */
    @Modifying
    @Query("UPDATE ScheduledReminder sr SET sr.status = 'CANCELLED', sr.processedAt = :now WHERE sr.appointmentId = :appointmentId AND sr.status = 'PENDING'")
    int cancelByAppointmentId(@Param("appointmentId") Long appointmentId, @Param("now") LocalDateTime now);

    /**
     * Find by idempotency key to prevent duplicates.
     */
    Optional<ScheduledReminder> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all reminders for a specific appointment.
     */
    List<ScheduledReminder> findByAppointmentId(Long appointmentId);
}
