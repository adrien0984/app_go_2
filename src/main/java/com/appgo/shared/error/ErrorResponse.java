package com.appgo.shared.error;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Réponse d'erreur standard pour l'API.
 */
public class ErrorResponse {

    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final List<ErrorDetail> errorDetails;
    private final String requestId;

    public ErrorResponse(String code, String message, LocalDateTime timestamp,
            List<ErrorDetail> errorDetails, String requestId) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
        this.errorDetails = errorDetails;
        this.requestId = requestId;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @JsonProperty("details")
    public List<ErrorDetail> getErrorDetails() {
        return errorDetails;
    }

    @JsonProperty("request_id")
    public String getRequestId() {
        return requestId;
    }

    public static class ErrorDetail {
        private final String field;
        private final String code;
        private final String message;

        public ErrorDetail(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

}

