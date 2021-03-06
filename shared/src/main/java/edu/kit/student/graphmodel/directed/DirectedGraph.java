package edu.kit.student.graphmodel.directed;

import edu.kit.student.graphmodel.Graph;
import edu.kit.student.graphmodel.Vertex;

import java.util.Set;

/**
 * A {@link DirectedGraph} is a specific Graph which contains only
 * {@link DirectedEdge} in its edge set.
 */
public interface DirectedGraph extends Graph {

	/**
	 * Returns the outdegree of a vertex of the graph.
	 * 
	 * @param vertex
	 *            Vertex whose outdegree will be returned.
	 * @return The number of edges going out of the supplied vertex.
	 */
	public Integer outdegreeOf(Vertex vertex);

	/**
	 * Returns the indegree of a vertex of the graph.
	 * 
	 * @param vertex
	 *            Vertex whose indegree will be returned.
	 * @return The number of edges going into the supplied vertex.
	 */
	public Integer indegreeOf(Vertex vertex);

	/**
	 * Returns the number of self loops of a vertex of the graph.
	 *
	 * @param vertex Vertex whose number of self loops will be returned.
	 * @return The number of edges who are self loops of the supplied Vertex.
     */
	public Integer selfLoopNumberOf(Vertex vertex);

	/**
	 * Returns a set of all outgoing edges of a vertex.
	 * 
	 * @param vertex
	 *            Vertex whose outgoing edges will be returned.
	 * @return The edges going out of the supplied vertex.
	 */
	public Set<? extends DirectedEdge> outgoingEdgesOf(Vertex vertex);

	/**
	 * Returns a set of all incoming edges of a vertex.
	 * 
	 * @param vertex
	 *            Vertex whose incoming edges will be returned.
	 * @return The edges coming in the supplied vertex.
	 */
	public Set<? extends DirectedEdge> incomingEdgesOf(Vertex vertex);

	/**
	 * Returns all self loops of a Vertex.
	 * @param vertex Vertex whose self loops will be returned.
	 * @return The Edges that are self loops of the supplied Vertex
     */
	public Set<? extends DirectedEdge> selfLoopsOf(Vertex vertex);

	@Override
	public Set<? extends Vertex> getVertexSet();

	@Override
	public Set<? extends DirectedEdge> getEdgeSet();
}
