package com.kerflowapp.kerflow.exceptions;

import lombok.Getter;

@Getter
public class KerflowException extends RuntimeException {

    private final ErrorCode code;
    private final String message;

    public KerflowException(ErrorCode code) {
        super(code.name());
        this.code = code;
        this.message = code.name();
    }

    public KerflowException(ErrorCode code, String message) {
        super(code.name());
        this.code = code;
        this.message = message;
    }

    public enum ErrorCode {
        VERIFICATION_CODE_EXPIRED,
        INVALID_VERIFICATION_CODE,
        USER_ALREADY_CONFIRMED,
        EMAIL_SENDING,
        INVALID_ACTION,
        INVALID_TOKEN,
        INVALID_USER_OR_PASSWORD,
        PROSPECT_NOT_FOUND,
        UNAUTHORIZED,
        USER_ALREADY_EXISTS,
        USER_CANNOT_ACCESS_THE_RESOURCE,
        USER_NOT_ENABLED,
        USER_NOT_FOUND,
        USER_PENDING_CONFIRMATION,
        CSV_IMPORT_FAILED,
        PIPELINE_COLUMN_NOT_FOUND,
        PIPELINE_COLUMN_NOT_EMPTY,
        PIPELINE_COLUMN_INVALID,
        PROSPECT_INVALID_STATUS,
        MESSAGE_NOT_FOUND,
        MESSAGE_NOT_OUTBOUND,
        STRIPE_ERROR,
        CHECKOUT_SESSION_INVALID
    }

}
