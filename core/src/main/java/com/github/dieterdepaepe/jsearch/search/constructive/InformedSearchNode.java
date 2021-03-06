package com.github.dieterdepaepe.jsearch.search.constructive;

/**
 * A container that contains a {@link SearchNode} and an estimate about the remaining cost before that node can
 * reach a solution.
 *
 * @param <T> the type of the {@code SearchNode} contained in this class
 * @author Dieter De Paepe
 */
public class InformedSearchNode<T extends SearchNode> implements Comparable<InformedSearchNode<T>> {
    private final T searchNode;
    private final Cost estimatedRemainingCost;

    /**
     * Creates a new instance.
     * @param searchNode a node
     * @param estimatedRemainingCost an estimate of the remaining cost before the node can reach a solution
     */
    public InformedSearchNode(T searchNode, Cost estimatedRemainingCost) {
        this.searchNode = searchNode;
        this.estimatedRemainingCost = estimatedRemainingCost;
    }

    public T getSearchNode() {
        return searchNode;
    }

    public Cost getEstimatedRemainingCost() {
        return estimatedRemainingCost;
    }

    public Cost getEstimatedTotalCost() {
        return getSearchNode().getCost().add(getEstimatedRemainingCost());
    }

    @Override
    public int compareTo(InformedSearchNode<T> o) {
        return getEstimatedTotalCost().compareTo(o.getEstimatedTotalCost());
    }
}
