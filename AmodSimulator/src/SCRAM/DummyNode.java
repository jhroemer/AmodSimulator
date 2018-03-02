package SCRAM;

public class DummyNode implements Node {
    private boolean visited;
    private Node previous;

    @Override
    public int getDistance(Node node) {
        return 0;
    }

    @Override
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public boolean isVisited() {
        return visited;
    }

    @Override
    public Node getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    @Override
    public String getInfo() {
        return "DummyNode";
    }
}
