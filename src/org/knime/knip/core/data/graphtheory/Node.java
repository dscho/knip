package org.knime.knip.core.data.graphtheory;

/**
 * Class to represent a pixel as a node in a graph.
 * 
 * This implementation was <b>heavily</b> inspired by the implementation provided by Kolmogorov and Boykov: MAXFLOW
 * version 3.01.
 * 
 * From the README of the library:
 * 
 * This software library implements the maxflow algorithm described in
 * 
 * "An Experimental Comparison of Min-Cut/Max-Flow Algorithms for Energy Minimization in Vision." Yuri Boykov and
 * Vladimir Kolmogorov. In IEEE Transactions on Pattern Analysis and Machine Intelligence (PAMI), September 2004
 * 
 * This algorithm was developed by Yuri Boykov and Vladimir Kolmogorov at Siemens Corporate Research. To make it
 * available for public use, it was later reimplemented by Vladimir Kolmogorov based on open publications.
 * 
 * If you use this software for research purposes, you should cite the aforementioned paper in any resulting
 * publication.
 * 
 * @author Jan Funke <jan.funke@inf.tu-dresden.de>
 * @version 0.1
 */

public class Node {

    // first outgoing edge
    private Edge m_firstOutgoing;

    // parent (in the tree structure)
    private Edge m_parent;

    // next active node
    private Node m_next;

    // timestamp indicating when distance was computed
    private int m_timestamp;

    // distance to the terminal
    private int m_distance;

    // indicates whether this node belongs to the sink or the source tree
    private boolean m_inSink;

    // indicates whether this node was changed
    private boolean m_marked;

    // indicates whether this node is in the changed list
    private boolean m_inChangedList;

    // the residual capacity of this node to the sink (<0) or from the
    // source
    // (>0)
    private float m_residualCapacity;

    public Node() {

        m_firstOutgoing = null;
        m_parent = null;
        m_next = null;

        m_timestamp = 0;
        m_distance = 0;
        m_inSink = false;
        m_marked = false;

        m_residualCapacity = 0;
    }

    /**
     * Gets the first outgoing node of this node.
     * 
     * @return The first outgoing node
     */
    public Edge getFirstOutgoing() {
        return this.m_firstOutgoing;
    }

    /**
     * Sets the firstOutgoing for this instance.
     * 
     * @param firstOutgoing The firstOutgoing.
     */
    public void setFirstOutgoing(final Edge firstOutgoing) {
        this.m_firstOutgoing = firstOutgoing;
    }

    /**
     * Gets the parent of this node in the tree structure
     * 
     * @return The parent of this node.
     */
    public Edge getParent() {
        return this.m_parent;
    }

    /**
     * Sets the parent for this node.
     * 
     * @param parent The new parent.
     */
    public void setParent(final Edge parent) {
        this.m_parent = parent;
    }

    /**
     * Gets the next active node.
     * 
     * @return The next active node.
     */
    public Node getNext() {
        return this.m_next;
    }

    /**
     * Sets the next node for this node.
     * 
     * @param next The next node.
     */
    public void setNext(final Node next) {
        this.m_next = next;
    }

    /**
     * Gets the timestamp for this node.
     * 
     * @return The timestamp.
     */
    public int getTimestamp() {
        return this.m_timestamp;
    }

    /**
     * Sets the timestamp for this instance.
     * 
     * @param timestamp The timestamp.
     */
    public void setTimestamp(final int timestamp) {
        this.m_timestamp = timestamp;
    }

    /**
     * Gets the distance of this node to source/sink.
     * 
     * @return The distance.
     */
    public int getDistance() {
        return this.m_distance;
    }

    /**
     * Sets the distance of this node to source/sink.
     * 
     * @param distance The distance.
     */
    public void setDistance(final int distance) {
        this.m_distance = distance;
    }

    /**
     * Determines if this node is connected to the sink.
     * 
     * @return <tt>true</tt>, if this node is connected to the sink.
     */
    public boolean isInSink() {
        return this.m_inSink;
    }

    /**
     * Sets whether or not this instance is connected to the sink.
     * 
     * @param inSink <tt>true</tt>, if this node is connected to the sink.
     */
    public void setInSink(final boolean inSink) {
        this.m_inSink = inSink;
    }

    /**
     * Determines if this node is marked.
     * 
     * @return <tt>true</tt>, if this node is marked.
     */
    public boolean isMarked() {
        return this.m_marked;
    }

    /**
     * Sets whether or not this instance is marked.
     * 
     * @param marked <tt>true</tt> to mark this node
     */
    public void setMarked(final boolean marked) {
        this.m_marked = marked;
    }

    /**
     * Sets the residualCapacity for this node.
     * 
     * @param residualCapacity The residual capacity.
     */
    public void setResidualCapacity(final float residualCapacity) {
        this.m_residualCapacity = residualCapacity;
    }

    /**
     * Determines if this instance is in changedNodes.
     * 
     * @return <tt>true</tt>, if this node is in changedNodes.
     */
    public boolean isInChangedList() {
        return this.m_inChangedList;
    }

    /**
     * Sets whether or not this instance is in changedNodes.
     * 
     * @param inChangedList <tt>true</tt>, if this node is in changedNodes
     */
    public void setInChangedList(final boolean inChangedList) {
        this.m_inChangedList = inChangedList;
    }

    /**
     * Gets the residual capacity for this node.
     * 
     * @return The residual capacity.
     */
    public float getResidualCapacity() {
        return this.m_residualCapacity;
    }
}
