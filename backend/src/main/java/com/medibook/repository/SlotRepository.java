package com.medibook.repository;

import com.medibook.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    // ── Provider Views ──────────────────────────────────────────────────

    List<Slot> findByProviderIdAndDate(Long providerId, LocalDate date);

    List<Slot> findByProviderIdOrderByDateAscStartTimeAsc(Long providerId);

    // ── Patient View: only available (not booked, not blocked) ─────────

    @Query("SELECT s FROM Slot s WHERE s.provider.id = :providerId AND s.date = :date " +
           "AND s.isBlocked = false ORDER BY s.startTime ASC")
    List<Slot> findAvailableSlots(@Param("providerId") Long providerId,
                                  @Param("date") LocalDate date);

    // ── All available slots for provider (patient calendar — next N days) ──

    @Query("SELECT s FROM Slot s WHERE s.provider.id = :providerId " +
           "AND s.date >= :fromDate AND s.isBlocked = false " +
           "ORDER BY s.date ASC, s.startTime ASC")
    List<Slot> findAvailableSlotsFrom(@Param("providerId") Long providerId,
                                      @Param("fromDate") LocalDate fromDate);

    // ── Stats ───────────────────────────────────────────────────────────

    long countByProviderId(Long providerId);

    long countByProviderIdAndIsBookedTrue(Long providerId);

    // ── Overlap detection ───────────────────────────────────────────────

    @Query("SELECT s FROM Slot s WHERE s.provider.id = :providerId AND s.date = :date " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<Slot> findOverlapping(@Param("providerId") Long providerId,
                                @Param("date") LocalDate date,
                                @Param("startTime") java.time.LocalTime startTime,
                                @Param("endTime") java.time.LocalTime endTime);

    // ── Cleanup: past slots ────────────────────────────────────

    @Query("SELECT s FROM Slot s WHERE s.date < :today OR (s.date = :today AND s.endTime <= :now)")
    List<Slot> findExpiredSlots(@Param("today") LocalDate today, @Param("now") java.time.LocalTime now);

    @Modifying
    @Query("DELETE FROM Slot s WHERE s.id = :id")
    void deleteSlotById(@Param("id") Long id);
}
