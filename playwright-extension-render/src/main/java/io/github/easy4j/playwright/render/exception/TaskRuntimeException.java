package io.github.easy4j.playwright.render.exception;



/**
 * Exception thrown when a rendering task fails during execution.
 */
public class TaskRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -7545341502620139031L;

    public TaskRuntimeException(String errorCode){
        super(errorCode);
    }

    public TaskRuntimeException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    public TaskRuntimeException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    public TaskRuntimeException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    public TaskRuntimeException(Throwable cause){
        super(cause);
    }
}
