package vkalashnykov.org.busapplication.api.domain;

public class Message {
    private String content;
    private String addedDate;

    public Message() {
    }

    public Message(String content, String addedDate) {
        this.content = content;
        this.addedDate = addedDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
}
