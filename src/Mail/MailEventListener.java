package Mail;

public interface MailEventListener {

    public enum Status {
        SUCCESS,
        CANCELLED
    }

    void mailSent(Status status);
}
