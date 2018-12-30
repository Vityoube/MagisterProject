package vkalashnykov.org.busapplication.api.domain;

public class BusInformation {
    private int busSize;
    private int trunkCapacity;
    private int salonCapacity;
    private int minSeats;
    private int fullNumberSeats;
    private int occupiedSeats;
    private int occupiedTrunk;
    private int occupiedSalonTrunk;

    public BusInformation() {
    }

    public BusInformation(int busSize, int trunkCapacity, int salonCapacity, int minSeats,
                          int fullNumberSeats) {
        this.busSize = busSize;
        this.trunkCapacity = trunkCapacity;
        this.salonCapacity = salonCapacity;
        this.minSeats = minSeats;
        this.fullNumberSeats = fullNumberSeats;
        occupiedSeats=0;
        occupiedTrunk=0;
        occupiedSalonTrunk=0;
    }

    public BusInformation(int busSize, int trunkCapacity, int salonCapacity, int minSeats,
                          int fullNumberSeats, int occupiedSeats, int occupiedTrunk, int occupiedSalonTrunk) {
        this.busSize = busSize;
        this.trunkCapacity = trunkCapacity;
        this.salonCapacity = salonCapacity;
        this.minSeats = minSeats;
        this.fullNumberSeats = fullNumberSeats;
        this.occupiedSeats = occupiedSeats;
        this.occupiedTrunk = occupiedTrunk;
        this.occupiedSalonTrunk = occupiedSalonTrunk;
    }

    public int getBusSize() {
        return busSize;
    }

    public void setBusSize(int busSize) {
        this.busSize = busSize;
    }

    public int getTrunkCapacity() {
        return trunkCapacity;
    }

    public void setTrunkCapacity(int trunkCapacity) {
        this.trunkCapacity = trunkCapacity;
    }

    public int getSalonCapacity() {
        return salonCapacity;
    }

    public void setSalonCapacity(int salonCapacity) {
        this.salonCapacity = salonCapacity;
    }

    public int getMinSeats() {
        return minSeats;
    }

    public void setMinSeats(int minSeats) {
        this.minSeats = minSeats;
    }

    public int getFullNumberSeats() {
        return fullNumberSeats;
    }

    public void setFullNumberSeats(int fullNumberSeats) {
        this.fullNumberSeats = fullNumberSeats;
    }

    public int getOccupiedSeats() {
        return occupiedSeats;
    }

    public void setOccupiedSeats(int occupiedSeats) {
        this.occupiedSeats = occupiedSeats;
    }

    public int getOccupiedTrunk() {
        return occupiedTrunk;
    }

    public void setOccupiedTrunk(int occupiedTrunk) {
        this.occupiedTrunk = occupiedTrunk;
    }

    public int getOccupiedSalonTrunk() {
        return occupiedSalonTrunk;
    }

    public void setOccupiedSalonTrunk(int occupiedSalonTrunk) {
        this.occupiedSalonTrunk = occupiedSalonTrunk;
    }
}
