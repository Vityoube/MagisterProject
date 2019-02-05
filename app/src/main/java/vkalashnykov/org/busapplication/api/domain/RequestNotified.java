package vkalashnykov.org.busapplication.api.domain;

public class RequestNotified {
    private Request request;

    public RequestNotified() {
    }

    public RequestNotified(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestNotified that = (RequestNotified) o;

        return request != null ? request.equals(that.request) : that.request == null;
    }
}
