package vkalashnykov.org.busapplication.api.domain;

public class Distance {
    private Position from;
    private Position to;
    private long time;

    public Distance(Position from, Position to) {
        this.from = from;
        this.to = to;
        time=0;
    }

    public Distance() {
    }

    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Distance distance = (Distance) o;

        return this.from.equals(distance.getFrom()) && this.to.equals(distance.getTo());
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
