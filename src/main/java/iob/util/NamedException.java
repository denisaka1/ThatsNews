package iob.util;

import java.util.Arrays;

/**
 * A custom exception for better output and specific exception handling.
 */
public class NamedException extends RuntimeException {

    private static final long serialVersionUID = 8184912065817039132L;
    private final String FULL_MESSAGE;
    private final String SIMPLE_MESSAGE;

    public NamedException(String _msg, Object... args) {
        this(_msg + " : " + Arrays.toString(args));
    }

    public NamedException(String _msg) {
        super(_msg);
        FULL_MESSAGE = _msg;
        SIMPLE_MESSAGE = FULL_MESSAGE;
    }

    public NamedException(Throwable throwable) {
        super(throwable);
        SIMPLE_MESSAGE = getRootCause(throwable).toString();
        FULL_MESSAGE = throwable + " (" + SIMPLE_MESSAGE + ")";
    }

    public String getSimpleMessage() {
        return SIMPLE_MESSAGE;
    }

    public String getFullMessage() {
        return FULL_MESSAGE;
    }

    /**
     * Get the root cause of a throwable by traversing its cause stack.
     *
     * @param throwable the throwable we investigate.
     * @return the root throwable cause for the input throwable.
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause;
        Throwable result = throwable;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }
}