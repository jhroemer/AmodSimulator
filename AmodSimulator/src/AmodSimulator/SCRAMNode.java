package AmodSimulator;

public class SCRAMNode {
    private boolean visited;
    private SCRAMNode previous;

    public SCRAMNode() {
        visited = false;
        previous = null;
    }

    public boolean isVisited() {
        return visited;
    }

    public SCRAMNode getPrevious() {
        return previous;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void setPrevious(SCRAMNode previous) {
        this.previous = previous;
    }
}
