package com.github.dieterdepaepe.jsearch.search.statespace.solver;

import static org.testng.Assert.*;

import com.github.dieterdepaepe.jsearch.datastructure.lightweight.SingleLinkedListing;
import com.github.dieterdepaepe.jsearch.problem.dummy.DummyGenerator;
import com.github.dieterdepaepe.jsearch.problem.dummy.DummyHeuristic;
import com.github.dieterdepaepe.jsearch.problem.dummy.DummySearchNode;
import com.github.dieterdepaepe.jsearch.problem.npuzzle.*;
import com.github.dieterdepaepe.jsearch.search.statespace.*;
import com.github.dieterdepaepe.jsearch.search.statespace.dev.LoggingGenerator;
import com.github.dieterdepaepe.jsearch.search.statespace.util.BasicManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Testing class for {@link com.github.dieterdepaepe.jsearch.search.statespace.solver.AStarSolver}.
 */
public class AStarSolverTest {
    @Test
    public void testSolvesNPuzzle() {
        List<Move> moves1 = Arrays.asList();
        List<Move> moves2 = Arrays.asList(Move.UP, Move.LEFT, Move.LEFT, Move.DOWN);
        List<Move> moves3 = Arrays.asList(Move.LEFT, Move.LEFT, Move.UP, Move.RIGHT, Move.DOWN, Move.LEFT);

        for (List<Move> moves : Arrays.asList(moves1, moves2, moves3)) {
            SlidingPuzzle puzzle = new SlidingPuzzle(3);
            PuzzleFields targetFieldState = puzzle.createFields();
            PuzzleEnvironment environment = new PuzzleEnvironment(puzzle, targetFieldState);
            ManhattanDistance heuristic = new ManhattanDistance();
            PuzzleSearchNodeGenerator nodeGenerator = new PuzzleSearchNodeGenerator();

            PuzzleFields startFieldState = targetFieldState;
            for (Move move : moves)
                startFieldState = puzzle.move(startFieldState, move);

            Solver solver = new AStarSolver();
            BasicManager<PuzzleSearchNode> manager = new BasicManager<>();

            assertTrue(puzzle.canReach(startFieldState, targetFieldState));
            solver.solve(
                    nodeGenerator.createStartState(startFieldState, environment, heuristic),
                    environment,
                    heuristic,
                    nodeGenerator,
                    manager
            );

            Solution<? extends PuzzleSearchNode> solution = manager.getSolution();

            assertTrue(solution.isOptimal());
            assertEquals(solution.getNode().getCost(), 0. + moves.size());
            assertEquals(solution.getNode().getPuzzleFields(), targetFieldState);
            assertEquals(SingleLinkedListing.toList(solution.getNode().getMoves(), false), getInverseMoves(moves));
        }
    }

    private static List<Move> getInverseMoves(List<Move> moves) {
        List<Move> result = new ArrayList<>(moves.size());
        for (int i = moves.size() - 1; i >= 0; i--)
            result.add(Move.inverse(moves.get(i)));
        return result;
    }

    @Test
    public void testNodeExpansionOrder() {
        // Search space for this test, nodes are ordered from cheap to expensive, goals nodes are written in capitals.
        //  ----a----
        //  |   |   b
        //  c   |   |
        // / \  d   |
        // e |  |   |
        //   |  F   |
        //   g      |
        //   |      H
        //   I

        DummySearchNode a = new DummySearchNode(0, 0, false);
        DummySearchNode b = new DummySearchNode(1, 0, false);
        DummySearchNode c = new DummySearchNode(0.5, 1.5, false);
        DummySearchNode d = new DummySearchNode(2, 1, false);
        DummySearchNode e = new DummySearchNode(0, 4, false);
        DummySearchNode f = new DummySearchNode(5, 0, true);
        DummySearchNode g = new DummySearchNode(4, 2, false);
        DummySearchNode h = new DummySearchNode(7, 0, true);
        DummySearchNode i = new DummySearchNode(8, 0, true);

        ListMultimap<DummySearchNode, DummySearchNode> successors = ArrayListMultimap.create();
        successors.put(a, c);
        successors.put(a, d);
        successors.put(a, b);
        successors.put(c, e);
        successors.put(c, g);
        successors.put(g, i);
        successors.put(d, f);
        successors.put(b, h);

        LoggingGenerator<DummySearchNode, Object> generator = new LoggingGenerator<>(new DummyGenerator(successors));
        DummyHeuristic heuristic = new DummyHeuristic();
        BasicManager<DummySearchNode> manager = new BasicManager<>();
        AStarSolver solver = new AStarSolver();

        solver.solve(new InformedSearchNode<>(a, 0), null, heuristic, generator, manager);

        assertEquals(generator.getExpandedNodes(), Arrays.asList(a, b, c, d, e));
        assertEquals(manager.getSolution().getNode(), f);
        assertTrue(manager.getSolution().isOptimal());
    }
}