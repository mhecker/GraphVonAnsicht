package edu.kit.student.graphmodel;

import java.util.List;

import edu.kit.student.objectproperty.GAnsProperty;
import javafx.scene.paint.Color;

/**
 * This edge interface specifies an edge. An edge contains two vertices, an ID,
 * a name and a label.
 */
public interface Edge {

	/**
	 * Returns the vertices connected with this edge.
	 * 
	 * @return The vertices connected with the edge.
	 */
	public List<? extends Vertex> getVertices();

	/**
	 * Returns the name of the edge.
	 * 
	 * @return The name of the edge.
	 */
	public String getName();

	/**
	 * Returns the ID of the edge.
	 * 
	 * @return The id of the edge.
	 */
	public Integer getID();

	/**
	 * Returns the label of the edge.
	 * 
	 * @return The label of the edge.
	 */
	public String getLabel();

	/**
	 * Adds the edge to a {@link FastGraphAccessor}.
	 * 
	 * @param fga The {@link FastGraphAccessor} to whom this edge will be added.
	 */
	public void addToFastGraphAccessor(FastGraphAccessor fga);
	
	/**
	 * Returns the {@link EdgePath} of the edge.
	 * The edge path is attached to the edge and cannot be replaced.
	 * 
	 * @return the edge path
	 */
	public EdgePath getPath();
	
	public EdgeArrow getArrowHead();

	/**
	 * Returns a list of properties of the edge that should be shown 
	 * in the InformationView when selected
	 * @return list of properties
	 */
	public List<GAnsProperty<?>> getProperties();
	
	public Color getColor();
}
