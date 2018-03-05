package SCRAM;

import AmodSimulator.Request;
import AmodSimulator.Vehicle;

public class Edge implements Comparable<Edge> {
    int weight;
    Node startNode;
    Node endNode;
    // for MacAlpine IndexBasedSCRAM implementation
    int startIndex;
    int endIndex;

    public Edge(Node start, Node end) {
        this.weight = start.getDistance(end);
        this.startNode = start;
        this.endNode = end;
    }

    /**
     * Constructor that works with the array-based IndexBasedSCRAM implementation
     *
     * @param start
     * @param end
     * @param weight
     */
    public Edge(int start, int end, int weight) {
        this.weight = weight;
        this.startIndex = start;
        this.endIndex = end;
    }

    /**
     *
     * @return
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     *
     * @return
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Needed for sorting
     *
     * @param other the opposite node
     * @return 1 if this node has larger weight than other, 0 if they're equal
     * and -1 if other has larger weight than this
     */
    @Override
    public int compareTo(Edge other) {
        //noinspection Duplicates
        if (this.weight < other.weight) return -1;
        else if (this.weight == other.weight) return 0;
        else return 1;
    }

    /**
     *
     * @return
     */
    public int getWeight() {
        return weight;
    }

    /**
     *
     * @param weight
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     *
     * @return
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     *
     * @return
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     *
     * @return true if a node in the edge is a dummynode
     */
    public boolean hasDummyNode() {
        return startNode instanceof DummyNode || endNode instanceof DummyNode;
    }

    /**
     *
     * @return
     */
    public Vehicle getVehicle() {
        if (startNode instanceof Vehicle) return (Vehicle) startNode;
        else try {
            throw new Exception("getVehicle was called on an edge where the start-node was not a vehicle");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @return
     */
    public Request getRequest() {
        if (endNode instanceof Request) return (Request) endNode;
        else try {
            throw new Exception("getRequest was called on an edge where the end-node was not a request");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}