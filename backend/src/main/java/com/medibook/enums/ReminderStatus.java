package com.medibook.enums;

/**
 * Lifecycle status of a scheduled reminder.
 * Transitions: PENDING → SENT | CANCELLED | FAILED
 */
public enum ReminderStatus {
    PENDING,
    SENT,
    CANCELLED,
    FAILED
}
