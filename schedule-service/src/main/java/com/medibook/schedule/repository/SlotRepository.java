package com.medibook.schedule.repository;

import com.medibook.schedule.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByProviderIdAndDate(Long providerId, LocalDate date);

    List<Slot> findByProviderIdOrderByDateAscStartTimeAsc(Long providerId);

    @Query("SELECT s FROM Slot s WHERE s.providerId = :providerId AND s.date = :date " +
           "AND s.isBlocked = false ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlots(@Param("providerId") Long providerId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Slot s WHERE s.providerId = :providerId " +
           "AND s.date >= :fromDate AND s.isBlocked = false " +
           "ORDER BY s.date ASC, s.startTime ASC")
    List<Slot> findAvailableSlotsFrom(@Param("providerId") Long providerId, @Param("fromDate") LocalDate fromDate);

    long countByProviderId(Long providerId);

    long countByProviderIdAndIsBookedTrue(Long providerId);

    @Query("SELECT s FROM Slot s WHERE s.providerId = :providerId AND s.date = :date " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Slot> findOverlapping(@Param("providerId") Long providerId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Slot s WHERE s.date < :today OR (s.date = :today AND s.endTime <= :now)")
    List<Slot> findExpiredSlots(@Param("today") LocalDate today, @Param("now") LocalTime now);

    @Modifying
    @Query("DELETE FROM Slot s WHERE s.id = :id")
    void deleteSlotById(@Param("id") Long id);
}
