package edu.kit.student.sugiyama.steps;

import edu.kit.student.parameter.*;
import edu.kit.student.sugiyama.graph.ICrossMinimizerGraph;
import edu.kit.student.sugiyama.graph.ISugiyamaEdge;
import edu.kit.student.sugiyama.graph.ISugiyamaVertex;
import edu.kit.student.sugiyama.graph.SugiyamaGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This class takes a Sugiyama Graph and rearranges its vertices on each layer to minimize
 * the amount of edge crossings.
 */ 
public class CrossMinimizer implements ICrossMinimizer {
	private double crossingReductionThreshold;
	private int maxRuns;
	private boolean stopOnThreshold = true;
	private boolean intelligentThreshold = false;
	private Settings settings;

    final Logger logger = LoggerFactory.getLogger(CrossMinimizer.class);

	public CrossMinimizer() {
		this(0.001f, 10, false);
	}

	public CrossMinimizer(float crossingReductionThreshold, int maxRuns, boolean stopOnThreshold) {
		setCrossingReductionThreshold(crossingReductionThreshold);
		setMaxRuns(maxRuns);
		this.stopOnThreshold = stopOnThreshold;
	}

	private void setCrossingReductionThreshold(double crossingReductionThreshold) {
		this.crossingReductionThreshold = Math.min(Math.max(crossingReductionThreshold, 0.0000000000001d), 1d);
	}

	private void setMaxRuns(int maxRuns) {
		this.maxRuns = Math.min(Math.max(maxRuns, 1), 99999999);
	}

	public void setStopOnThreshold(boolean stopOnThreshold) {
		this.stopOnThreshold = stopOnThreshold;
	}

	@Override
	public void minimizeCrossings(ICrossMinimizerGraph graph) {
		if (settings != null) {
			this.stopOnThreshold = Settings.unpackBoolean((Parameter<?, Boolean>) getSettings().get("Use Threshold"));
			setCrossingReductionThreshold(Settings.unpackDouble((Parameter<?, Double>) getSettings().get("Crossminimizer reduction Threshold")));
			setMaxRuns(Settings.unpackInteger((Parameter<?, Integer>) getSettings().get("Crossminimizer max runs")));
		}

		logger.info("CrossMinimizer.minimizeCrossings():");
		logger.info("stop on Threshold: " + stopOnThreshold);
		logger.info("threshold: " + crossingReductionThreshold);
		logger.info("max runs: " + maxRuns);

		int layerCount = graph.getLayerCount();

		addDummyAndEdges(graph);
		//System.out.println("crossings: " + crossings((SugiyamaGraph) graph));

		int newCrossings = 0;
		int oldCrossings = 0;
		int counter = 0;

		if (stopOnThreshold) {
			oldCrossings = crossings((SugiyamaGraph) graph);
		}

		while (counter < maxRuns) {
			List<List<ISugiyamaVertex>> undo = new ArrayList<>();

			for (List<ISugiyamaVertex> layer : graph.getLayers()) {
				undo.add(new ArrayList<ISugiyamaVertex>(layer));
			}

			//System.out.println("optimize up");
			for (int i = 1; i < layerCount; i++) {
				optimizeLayer(graph, i, Direction.UP);
			}
			//System.out.println("optimize down");
			for (int i = layerCount - 2; i >= 0; i--) {
				optimizeLayer(graph, i, Direction.DOWN);
			}
			counter++;

			if (stopOnThreshold) {
				newCrossings = crossings((SugiyamaGraph) graph);
			}


			if (oldCrossings - newCrossings < 0) {
			    int y = 0;
			    for (List<ISugiyamaVertex> layer : undo) {
			        graph.setPositionsOnLayer(y, layer);
			        y++;
			    }
				break;
			}

			if (stopOnThreshold && newCrossings == 0 || oldCrossings - newCrossings < crossingReductionThreshold * oldCrossings) {
				break;
			}

			oldCrossings = newCrossings;
		}

		logger.debug(counter + " runs");
		//System.out.println("crossings: " + crossings((SugiyamaGraph) graph));
		//for printing the layers after cross minimization
		graph.getLayers().forEach(iSugiyamaVertices -> logger.debug(iSugiyamaVertices.stream().map(iSugiyamaVertex -> iSugiyamaVertex.getName()).collect(Collectors.joining(", "))));
	}

	@Override
	public Settings getSettings() {
		if (this.settings != null) {
			return this.settings;
		}

		BooleanParameter p1 = new BooleanParameter("Use Threshold", false);
		DoubleParameter p2 = new DoubleParameter("Crossminimizer reduction Threshold", 0.001, 0.001, 0.01, 0.001);
		IntegerParameter p3 = new IntegerParameter("Crossminimizer max runs", 100, 1, 999999);

		//Needs to be a LinkedHashMap, because the parameters might need to be displayed in a specific order to make sense
		LinkedHashMap<String, Parameter<?,?>> parameter = new LinkedHashMap<String, Parameter<?,?>>();
		parameter.put(p1.getName(), p1);
		parameter.put(p2.getName(), p2);
		parameter.put(p3.getName(), p3);
		Settings settings = new Settings(parameter);
		this.settings = settings;
		return settings;
	}

	/**
	 * This method adds dummy vertices between the two vertices of an edge, on every layer that this edge skips.
	 * the dummy vertices are connected through supplement edges with each other and the source and target vertex of the edge.
	 * 
	 * @param graph input graph to add dummy vertices and supplement edges to
	 */
	private void addDummyAndEdges(ICrossMinimizerGraph graph) {
		Set<ISugiyamaVertex> vertices = graph.getVertexSet();
		Set<ISugiyamaEdge> edges = graph.getEdgeSet();
		Set<ISugiyamaEdge> newEdges = new HashSet<ISugiyamaEdge>();
		Set<ISugiyamaEdge> replacedEdges = new HashSet<ISugiyamaEdge>();

		for(ISugiyamaEdge edge : edges){
			ISugiyamaVertex source = edge.getSource();
			ISugiyamaVertex target = edge.getTarget();

			if (Objects.equals(source.getID(), target.getID())) {
				continue;
			}

			int lowerLayer = source.getLayer();
			int upperLayer = target.getLayer();
			int diff = upperLayer - lowerLayer;
			assert(diff >= 0);	//diff must not be lower than one, both vertices must not be on the same layer!
			assert(graph.getLayer(lowerLayer).contains(source));
			assert(graph.getLayer(upperLayer).contains(target));

			if(diff>1){	//need to add #diff dummy vertices
				List<ISugiyamaVertex> dummies = new LinkedList<>();
				List<ISugiyamaEdge> supplementEdges = new LinkedList<>();
				replacedEdges.add(edge);		// the  distance of both vertices of this edge is greater than 1 so it must be replaced
				ISugiyamaVertex nv = null;	// through dummy vertices and supplement edges. add it here to remove it later from the original edge set.
				ISugiyamaEdge ne;
				int c = 0;

				for(int l = lowerLayer + 1; l <= upperLayer; l++){
					c++;
					ISugiyamaVertex dummy = null;

					if(l==lowerLayer+1){
						nv = graph.createDummy("d"+c+ '(' +source.getName()+"->"+target.getName()+ ')', "", lowerLayer + 1);	//first dummy vertex created
						dummy = nv;
						ne = graph.createSupplementEdge(edge.getName()+ '(' +c+ ')', "", source, nv);	//first dummy edge created
						supplementEdges.add(ne);
						((SugiyamaGraph) graph).assignToLayer(nv, l);
					}else if(l==upperLayer){
						ne = graph.createSupplementEdge(edge.getName() + "(e" + c + ')', "", nv, target);
						supplementEdges.add(ne);
					}else{
						ISugiyamaVertex temp = nv;	//temporary ISugiyamaVertex so that the new created vertex is always the one with the variable nv
						nv = graph.createDummy("d"+c+ '(' +source.getName()+"->"+target.getName()+ ')', "", c);
						dummy = nv;
						ne = graph.createSupplementEdge(edge.getName()+ '(' +c+ ')', "", temp, nv);
						supplementEdges.add(ne);
						((SugiyamaGraph) graph).assignToLayer(nv, l);
					}

					if (dummy != null) {
						dummies.add(dummy);
					}
				}

				graph.createSupplementPath(edge, dummies, supplementEdges);
			}
		}

		edges.removeAll(replacedEdges);	//remove all replaced edges from the original edge set
	}

	private int optimizeLayer(ICrossMinimizerGraph graph, int optimizingLayer, Direction dir) {
		int changes = 1;
		List<ISugiyamaVertex> layer = graph.getLayer(optimizingLayer);
		List<ISugiyamaVertex> newLayer;
		Map<ISugiyamaVertex, Float> barycenterMap = new HashMap<>();

		for (ISugiyamaVertex vertex : layer) {
			barycenterMap.put(vertex, getBarycenter(graph, vertex, dir));
		}

		//barycenterMap = layer.stream().parallel().collect(Collectors.toMap(vertex -> vertex, vertex -> getBarycenter(graph, vertex, dir)));

		newLayer = toSortedKeyList(barycenterMap);
		graph.setPositionsOnLayer(optimizingLayer, newLayer);

		return changes;
	}
	
	private float getBarycenter (ICrossMinimizerGraph graph, ISugiyamaVertex vertex, Direction dir) {
		Set<ISugiyamaVertex> relevantNeighbors;
		int optimizingLayerNum = vertex.getLayer();
		int fixedLayerNum;

		if (dir == Direction.DOWN) {
			relevantNeighbors = graph.outgoingEdgesOf(vertex).stream().map((ISugiyamaEdge -> ISugiyamaEdge.getTarget())).collect(Collectors.toSet());
			fixedLayerNum = optimizingLayerNum + 1;
		} else if (dir == Direction.UP) {
			fixedLayerNum = optimizingLayerNum - 1;
			relevantNeighbors = graph.incomingEdgesOf(vertex).stream().map((ISugiyamaEdge -> ISugiyamaEdge.getSource())).collect(Collectors.toSet());
		} else {
			throw new NullPointerException();
		}

		List<ISugiyamaVertex> fixedLayer = graph.getLayer(fixedLayerNum);
		OptionalDouble optionalAvarage = relevantNeighbors.stream().mapToDouble((vertex1 -> fixedLayer.indexOf(vertex1))).average();

		if (optionalAvarage.isPresent()) {
			return (float) optionalAvarage.getAsDouble();
		} else {
			return (float) graph.getLayer(optimizingLayerNum).indexOf(vertex);
		}
	}

	public static <K, V extends Comparable<? super V>> List<K> toSortedKeyList( Map<K, V> map ) {
		List<K> result = new ArrayList<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue()).forEachOrdered(e -> result.add((K) e.getKey()));

		return result;
	}

	private enum Direction {
		UP, DOWN;
	}

	public static int crossings(SugiyamaGraph graph) {
		int result;
		List<List<ISugiyamaVertex>> layers = graph.getLayers();


		result = IntStream.range(0, graph.getLayerCount() - 1)
				.parallel()
				.map(i -> {
					return crossingsOfLayers(graph, layers.get(i), layers.get(i + 1), i + 1);
				}).sum();

		return result;
	}

	public static int crossingsOfLayers(SugiyamaGraph graph, List<ISugiyamaVertex> layer1, List<ISugiyamaVertex> layer2, int layer2number) {
		if (layer1 == null || layer2 == null) {
			return 0;
		}

		int layer1size = layer1.size();
		if (layer1size <= 1) {
			return 0;
		}

		int result = 0;
		for (int i = layer1size - 1; --i >= 0;) {
			int k = i + 1;
			for (int j = layer1size - 1; --j >= k;) {
				result += crossingsOfVertices(graph, layer1.get(i), layer1.get(j), layer2, layer2number);
			}
		}

		return result;
	}

	private static int crossingsOfVertices(SugiyamaGraph graph, ISugiyamaVertex vertex1, ISugiyamaVertex vertex2, List<ISugiyamaVertex> layer, int layer2number) {
		if (Objects.equals(vertex1.getID(), vertex2.getID())) {
			return 0;
		}

		//System.out.println(vertex1.getName() + " X  " + vertex2.getName());

		Set<ISugiyamaEdge> vertex1Edges = graph.edgesOf(vertex1);

		vertex1Edges = vertex1Edges
				.stream()
				.filter(edge -> edge.getSource().getLayer() == layer2number || edge.getTarget().getLayer() == layer2number)
				.collect(Collectors.toSet());

		List<ISugiyamaVertex> sources = vertex1Edges.stream().map(sugiyamaEdge -> sugiyamaEdge.getSource()).filter(vertex -> !Objects.equals(vertex.getID(), vertex2.getID())).collect(Collectors.toList());
		List<ISugiyamaVertex> targets = vertex1Edges.stream().map(sugiyamaEdge -> sugiyamaEdge.getTarget()).filter(vertex -> !Objects.equals(vertex.getID(), vertex2.getID())).collect(Collectors.toList());
		//sources.removeIf(vertex -> vertex.getID() == vertex1.getID());
		//targets.removeIf(vertex -> vertex.getID() == vertex1.getID());

		List<ISugiyamaVertex> neighbors1 = sources;
		neighbors1.addAll(targets);

		Set<ISugiyamaEdge> vertex2Edges = graph.edgesOf(vertex2);

		vertex2Edges = vertex2Edges
				.stream()
				.filter(edge -> edge.getSource().getLayer() == layer2number || edge.getTarget().getLayer() == layer2number)
				.collect(Collectors.toSet());

		sources = vertex2Edges.stream().map(sugiyamaEdge -> sugiyamaEdge.getSource()).filter(vertex -> !Objects.equals(vertex.getID(), vertex2.getID())).collect(Collectors.toList());
		targets = vertex2Edges.stream().map(sugiyamaEdge -> sugiyamaEdge.getTarget()).filter(vertex -> !Objects.equals(vertex.getID(), vertex2.getID())).collect(Collectors.toList());
		//sources.removeIf(vertex -> vertex.getID() == vertex2.getID());
		//targets.removeIf(vertex -> vertex.getID() == vertex2.getID());

		List<ISugiyamaVertex> neighbors2 = sources;
		neighbors2.addAll(targets);

		int result = 0;

		for (ISugiyamaVertex x1 : neighbors1) {
			for (ISugiyamaVertex x2 : neighbors2) {
				//System.out.println("   " + x1.getName() + " ? " + x2.getName());
				if (graph.getPosition(x1).x > graph.getPosition(x2).x) {
					//System.out.println("  hit");
					result++;
				}
			}
		}

		//result = neighbors1.stream().parallel().mapToInt(n1 -> neighbors2.stream().mapToInt(n2 -> layer.indexOf(n1) > layer.indexOf(n2) ? 1 : 0).sum()).sum();

		return result;
	}
}
