package AmodSimulator;

public class Assignment {

    private Vehicle vehicle;
    private Request request;
    private boolean isDummy;

    public Assignment() {}

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

    public boolean isDummy() {
        return isDummy;
    }

    public void setToDummy() {
        isDummy = true;
    }

    @Override
    public String toString() {
        if (isDummy) return "DummyAssignment";
        return "Assignment{" +
                "vehicle=" + vehicle.getId() +
                ", request=" + request.getId() +
                '}';
    }
}
