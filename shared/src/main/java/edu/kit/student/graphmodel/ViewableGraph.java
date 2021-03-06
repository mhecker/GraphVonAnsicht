package edu.kit.student.graphmodel;

import java.util.List;
import java.util.Set;

import edu.kit.student.graphmodel.action.SubGraphAction;
import edu.kit.student.graphmodel.action.VertexAction;
import edu.kit.student.objectproperty.GAnsProperty;
import edu.kit.student.plugin.EdgeFilter;
import edu.kit.student.plugin.LayoutOption;
import edu.kit.student.plugin.VertexFilter;

/**
 * The base graph accessed by the UI.
 */
public interface ViewableGraph extends Graph {

	/**
	 * Returns the name of the Graph.
	 * @return The name of the graph.
	 */
	public String getName();

	/**
	 * Returns the ID of the graph.
	 * @return The id of the graph.
	 */
	public Integer getID();
	
	/**
	 * Returns the statistics for this graph as a list of GansProperties.
	 * @return the statistics
	 */
	public List<GAnsProperty<?>> getStatistics();

	/**
	 * Returns a list of actions which can be executed on the specified vertex induced subgraph.
	 * @param vertices the vertices inducing the subgraph
	 * @return         the list of actions
	 */
	public List<SubGraphAction> getSubGraphActions(Set<ViewableVertex> vertices);

	/**
	 * Returns a list of actions which can be executed on the specified vertex.
	 * @param vertex the vertex
	 * @return       the list of actions
	 */
	public List<VertexAction> getVertexActions(Vertex vertex);
	
	/**
	 * Adds a vertex filter to the list of active filters for this graph.
	 * @param filter the filter to add
	 */
	public void addVertexFilter(VertexFilter filter);
	
	/**
	 * Sets  vertex filter from the collection to the list of active filters for this graph.
	 * @param filter the filter collection to add
	 */
	public void setVertexFilter(List<VertexFilter> filter);
	
	/**
	 * Returns a unmodifiable list of all active vertex filters for this graph.
	 * @return A list of all active vertex filters for this graph.
	 */
	public List<VertexFilter> getActiveVertexFilter();
	
	/**
	 * Adds an edge filter to the list of active filters for this graph.
	 * @param filter the filter to add
	 */
	public void addEdgeFilter(EdgeFilter filter);
	
	/**
	 * Adds all edge filter from the collection to the list of active filters for this graph.
	 * @param filter the filter collection to add
	 */
	public void setEdgeFilter(List<EdgeFilter> filter);
	
	/**
	 * Returns a unmodifiable list of all active edge filters for this graph.
	 * @return A list of all active edge filters for this graph.
	 */
	public List<EdgeFilter> getActiveEdgeFilter();
	
	/**
	 * Removes a vertex filter from the list of active filters
	 * @param filter the filter to remove
	 */
	public void removeVertexFilter(VertexFilter filter);
	
	/**
	 * Removes a edge filter from the list of active filters
	 * @param filter the filter to remove
	 */
	public void removeEdgeFilter(EdgeFilter filter);

	@Override
    public Set<? extends ViewableVertex> getVertexSet();

	/**
	 * Returns the set of all conceptual subgraphs in this graph
	 * @return the list of subgraphs
	 */
	public Set<? extends SubGraph> getSubGraphs();

	/**
	 * Returns a list of layouts which have been registered at the corresponding
	 * LayoutRegister for this graph type. The graph implementing this interface
	 * will be set as target of the LayoutOption.
	 * 
	 * @return A list of layouts which have been registered at the corresponding
	 *         LayoutRegister for this graph type.
	 */
	public List<LayoutOption> getRegisteredLayouts();

	/**
	 * Returns the default layout for this graph.
	 * This can be called when to quickly get a suiting layout without
	 * having to decide between multiple options.
	 * @return the default layout for this graph
	 */
	public LayoutOption getDefaultLayout();
}
