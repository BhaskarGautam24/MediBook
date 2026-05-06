package com.medibook.service;

/**
 * SMS notification channel interface.
 * Implementations can integrate with Twilio, AWS SNS, etc.
 */
public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
