package sjes.elasticsearch.common;

/**
 * Created by qinhailong on 15-12-2.
 */
public class ServiceException extends Exception {

    private String messageCode = "SJES_ELASTICSEARCH";

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public ServiceException() {
        super();
    }

    public ServiceException(String messageCode) {
        super();
        this.messageCode = messageCode;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public String getMessageCode() {
        return messageCode;
    }
}
