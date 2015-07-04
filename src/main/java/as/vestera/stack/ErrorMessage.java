package as.vestera.stack;

public class ErrorMessage extends Message {
    String error;

    ErrorMessage(Throwable cause) {
        super(cause.getMessage());
        error = cause.getClass().getSimpleName();
    }
}
