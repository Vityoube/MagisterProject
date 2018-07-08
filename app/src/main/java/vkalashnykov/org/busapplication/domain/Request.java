package vkalashnykov.org.busapplication.domain;

import java.util.ArrayList;
import java.util.Date;

public class Request {
    private String clientKey,driverKey;
    private ArrayList<Message> messages;
    private String createDate;
    private STATUS status;

    public enum STATUS {
        RAISED, REJECTED, CANCELLED, APPROVED
    }

    public Request() {
        messages=new ArrayList<>();
    }

    public Request(String clientKey, String driverKey) {
        this.clientKey = clientKey;
        this.driverKey = driverKey;
        messages=new ArrayList<>();
    }

    public Request(String clientKey,String driverKey, ArrayList<Message> messages,
                   String createDate, STATUS status) {
        this.clientKey = clientKey;
        this.driverKey = driverKey;
        this.messages = messages;
        this.createDate = createDate;
        this.status = status;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getDriverKey() {
        return driverKey;
    }

    public void setDriverKey(String driverKey) {
        this.driverKey = driverKey;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }
}
