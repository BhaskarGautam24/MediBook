package com.medibook.enums;

/**
 * How the patient pays.
 * UPI, CARD, WALLET, NETBANKING → online payment modes
 * CASH → pay at clinic (no upfront payment required)
 */
public enum PaymentMode {
    UPI,
    CARD,
    WALLET,
    NETBANKING,
    CASH
}
