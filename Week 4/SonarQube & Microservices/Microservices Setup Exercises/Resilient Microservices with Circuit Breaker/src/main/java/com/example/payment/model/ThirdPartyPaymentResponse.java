package com.example.payment.model;

/**
 * Domain model representing the raw response structure of the simulated Third Party API.
 */
public class ThirdPartyPaymentResponse {
    private String status;
    private String transactionId;
    private String provider;

    // No-arguments constructor
    public ThirdPartyPaymentResponse() {
    }

    // All-arguments constructor
    public ThirdPartyPaymentResponse(String status, String transactionId, String provider) {
        this.status = status;
        this.transactionId = transactionId;
        this.provider = provider;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "ThirdPartyPaymentResponse{" +
                "status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
}
