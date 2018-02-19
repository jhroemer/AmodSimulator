package AmodSimulator;

public class Assignment {

    private Vehicle vehicle;
    private Request request;

    public Assignment(Vehicle vehicle, Request request) {
        this.vehicle = vehicle;
        this.request = request;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
