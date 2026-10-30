package edu.eci.dosw.oficioya_users.exception;

/** Datos válidos en formato pero que incumplen una regla de negocio (400). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
