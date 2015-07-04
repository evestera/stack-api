package as.vestera.stack;

public class Message {
    String message;

    Message(String message, Object... args) {
        this.message = String.format(message, args);
    }
}
