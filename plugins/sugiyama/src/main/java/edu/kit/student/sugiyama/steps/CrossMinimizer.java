package edu.kit.student.sugiyama.steps;

import edu.kit.student.sugiyama.graph.ICrossMinimizerGraph;
import edu.kit.student.sugiyama.graph.SugiyamaGraph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class takes a Sugiyama Graph and rearranges its vertices on each layer to minimize
 * the amount of edge crossings.
 */ 
public class CrossMinimizer implements ICrossMinimizer {

	@Override
	public void minimizeCrossings(ICrossMinimizerGraph graph) {
		System.out.println("graph before minimization");
		for (int i = 0; i < graph.getLayerCount(); i++) {
			System.out.println(graph.getLayer(i).stream().map(vertex -> vertex.getName()).collect(Collectors.joining(", ")));
		}
		System.out.println(" ");

		//add dummy knots
		int changes = Integer.MAX_VALUE;
		int counter = 0;
		while (changes > 0 && counter < 10) {
			changes = 0;
			System.out.println("optimize up");
			for (int i = 0; i < graph.getLayerCount() - 1; i++) {
				changes += optimizeLayer(graph, i, Direction.UP);
			}
			System.out.println("optimize down");
			for (int i = graph.getLayerCount() - 1; i > 0; i--) {
				changes += optimizeLayer(graph, i, Direction.DOWN);
			}

			counter++;
		}
		System.out.println(" ");
		System.out.println("runs = " + counter);
		System.out.println(" ");
		System.out.println("graph after minimization");
		for (int i = 0; i < graph.getLayerCount(); i++) {
			System.out.println(graph.getLayer(i).stream().map(vertex -> vertex.getName()).collect(Collectors.joining(", ")));
		}
	}

	private int optimizeLayer(ICrossMinimizerGraph graph, int optimizingLayer, Direction dir) {
		int changes = 0;
		List<SugiyamaGraph.SugiyamaVertex> layer = graph.getLayer(optimizingLayer);
		List<Integer> currentPositions = new LinkedList<>();
		List<Integer> newPositions = new LinkedList<>();
		Map<Integer, Float> barycenterMap = new HashMap<>();

		for (SugiyamaGraph.SugiyamaVertex vertex : layer) {
			int index = layer.indexOf(vertex);
			currentPositions.add(index);
			barycenterMap.put(index, getBarycenter(graph, vertex, dir));
		}

		newPositions = toSortedKeyList(barycenterMap);

		//System.out.println(graph.getLayer(optimizingLayer).stream().map(vertex -> vertex.getName()).collect(Collectors.joining(", ")));

		for (int i = 0; i < newPositions.size(); i++) {
			int currentPosition = currentPositions.get(i);
			int newPosition = newPositions.get(i);
			List<SugiyamaGraph.SugiyamaVertex> layer1 = graph.getLayer(optimizingLayer);

			if (newPosition != currentPosition) {
				SugiyamaGraph.SugiyamaVertex currentVertex = layer1.get(currentPosition);
				SugiyamaGraph.SugiyamaVertex newVertex = layer1.get(newPosition);
				currentPositions.set(newPosition, currentPosition);
				currentPositions.set(currentPosition, newPosition);
				graph.swapVertices(currentVertex, newVertex);
				changes++;
				System.out.println(newPositions.toString());
				System.out.println(graph.getLayer(optimizingLayer).stream().map(vertex -> vertex.getName()).collect(Collectors.joining(", ")));
			}
		}

		return changes;
	}
	
	private float getBarycenter (ICrossMinimizerGraph graph, SugiyamaGraph.SugiyamaVertex vertex, Direction dir) {
		Set<SugiyamaGraph.SugiyamaVertex> relevantNeighbors;
		int optimizingLayerNum = vertex.getLayer();
		int fixedLayerNum;

		if (dir == Direction.UP) {
			relevantNeighbors = graph.outgoingEdgesOf(vertex).stream().map((sugiyamaEdge -> sugiyamaEdge.getTarget())).collect(Collectors.toSet());
			fixedLayerNum = optimizingLayerNum + 1;
		} else if (dir == Direction.DOWN) {
			fixedLayerNum = optimizingLayerNum - 1;
			relevantNeighbors = graph.incomingEdgesOf(vertex).stream().map((sugiyamaEdge -> sugiyamaEdge.getSource())).collect(Collectors.toSet());
		} else {
			throw new NullPointerException();
		}

		List<SugiyamaGraph.SugiyamaVertex> fixedLayer = graph.getLayer(fixedLayerNum);
		//System.out.println(vertex.getName() + ": " + relevantNeighbors.stream().map(vertex1 -> Integer.toString(fixedLayer.indexOf(vertex1))).collect(Collectors.joining(", ")));
		OptionalDouble optionalAvarage = relevantNeighbors.stream().mapToDouble((vertex1 -> fixedLayer.indexOf(vertex1))).average();

		if (optionalAvarage.isPresent()) {
			return (float) optionalAvarage.getAsDouble();
		} else {
			return (float) graph.getLayer(optimizingLayerNum).indexOf(vertex);
		}
	}

	public static <K, V extends Comparable<? super V>> List<K> toSortedKeyList( Map<K, V> map ) {
		List<K> result = new LinkedList<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.add((K) e.getKey()));

		return result;
	}

	private enum Direction {
		UP, DOWN;
	}
}
