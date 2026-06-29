package com.example.payment.model;

/**
 * Domain model representing the consolidated response returned by the Payment Controller.
 */
public class PaymentResponse {
    private String status;
    private String transactionId;
    private String provider;
    private String message;
    private boolean fallback;
    private long responseTimeMs;

    // No-arguments constructor
    public PaymentResponse() {
    }

    // All-arguments constructor
    public PaymentResponse(String status, String transactionId, String provider, String message, boolean fallback, long responseTimeMs) {
        this.status = status;
        this.transactionId = transactionId;
        this.provider = provider;
        this.message = message;
        this.fallback = fallback;
        this.responseTimeMs = responseTimeMs;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    // Builder Pattern implementation
    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    public static class PaymentResponseBuilder {
        private String status;
        private String transactionId;
        private String provider;
        private String message;
        private boolean fallback;
        private long responseTimeMs;

        public PaymentResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentResponseBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentResponseBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public PaymentResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public PaymentResponseBuilder fallback(boolean fallback) {
            this.fallback = fallback;
            return this;
        }

        public PaymentResponseBuilder responseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public PaymentResponse build() {
            return new PaymentResponse(status, transactionId, provider, message, fallback, responseTimeMs);
        }
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", provider='" + provider + '\'' +
                ", message='" + message + '\'' +
                ", fallback=" + fallback +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}
