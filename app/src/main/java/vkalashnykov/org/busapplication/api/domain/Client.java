package vkalashnykov.org.busapplication.api.domain;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private String username;
    private String firstName;
    private String lastName;
    private List<String> requestIds;

    public Client() {
    }

    public Client(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        requestIds=new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(List<String> requestIds) {
        this.requestIds = requestIds;
    }

    public String addRequestId(String requestId){
        if (requestIds==null)
            requestIds=new ArrayList<>();
        requestIds.add(requestId);
        return requestId;
    }
}
