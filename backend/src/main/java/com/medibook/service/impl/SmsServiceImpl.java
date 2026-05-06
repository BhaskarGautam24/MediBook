package com.medibook.service.impl;

import com.medibook.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.messaging-service-sid}")
    private String messagingServiceSid;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio initialized with Messaging Service SID: {}", messagingServiceSid);
    }

    @Async
    @Override
    public void sendSms(String phoneNumber, String textMessage) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            log.warn("Cannot send SMS: Phone number is empty.");
            return;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    messagingServiceSid,
                    textMessage
            ).create();
            log.info("SMS sent to {} | SID: {}", phoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("SMS Delivery Failed", e); // Let retry mechanism handle it
        }
    }
}
