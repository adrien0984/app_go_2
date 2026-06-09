package com.appgo.katago.port;

/**
 * Exception levée lors d'une erreur KataGo.
 */
public class KataGoException extends Exception {

    public KataGoException(String message) {
        super(message);
    }

    public KataGoException(String message, Throwable cause) {
        super(message, cause);
    }
}
