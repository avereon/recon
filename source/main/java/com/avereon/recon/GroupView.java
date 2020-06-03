package com.avereon.recon;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.CubicCurve;

public class GroupView extends BorderPane {

	private static final double OFFSET = 20;

	private static final Pos POS = Pos.TOP_CENTER;

	private final String key;

	private Orientation orientation;

	private Pane box;

	private GroupView dependency;

	private CubicCurve connector;

	public GroupView( String key, String group ) {
		getStyleClass().add( "group-view" );

		this.key = key;

		Label label = new Label( group );
		BorderPane.setAlignment( label, Pos.CENTER );
		setTop( label );
		setOrientation( Orientation.VERTICAL );
	}

	public ObservableList<Node> getViews() {
		return box.getChildren();
	}

	public String getKey() {
		return key;
	}

	public void updateOrientation() {
		long count = getViews().stream().mapToInt( n -> ((DeviceView)n).getDevice().getDevices().size() ).count();
		setOrientation( count > 0 ? Orientation.HORIZONTAL : Orientation.VERTICAL );
	}

	private void setOrientation( Orientation orientation ) {
		if( this.orientation == orientation ) return;

		Pane oldBox = box;
		if( orientation == Orientation.HORIZONTAL ) {
			box = new HBox();
			((HBox)box).setFillHeight( false );
			((HBox)box).setAlignment( POS );
		} else {
			box = new VBox();
			((VBox)box).setFillWidth( false );
			((VBox)box).setAlignment( POS );
		}
		if( oldBox != null ) box.getChildren().addAll( oldBox.getChildren() );
		setCenter( box );

		this.orientation = orientation;
		requestLayout();
	}

	public void linkToParent( GroupView dependency ) {
		this.dependency = dependency;

		CubicCurve curve = new CubicCurve();
		curve.getStyleClass().addAll( "group-connector" );
		curve.setManaged( false );
		curve.setViewOrder( 2 );
		curve.setFill( null );
		this.connector = curve;

		// The dependency group is in a different container so it's a bit more complicated
		dependency.boundsInParentProperty().addListener( ( v, o, n ) -> updateConnectorStart() );
		boundsInParentProperty().addListener( ( v, o, n ) -> updateConnectorEnd() );
	}

	private void updateConnectorStart() {
		Bounds bounds = dependency.getBoundsInParent();
		Point2D anchor = connector.sceneToLocal( dependency.getParent().localToScene( bounds.getCenterX(), bounds.getMaxY() ) );
		connector.setStartX( anchor.getX() );
		connector.setStartY( anchor.getY() );
		connector.setControlX1( anchor.getX() );
		connector.setControlY1( anchor.getY() + OFFSET );
	}

	private void updateConnectorEnd() {
		Bounds bounds = getBoundsInParent();
		connector.setControlX2( bounds.getCenterX() );
		connector.setControlY2( bounds.getMinY() - OFFSET );
		connector.setEndX( bounds.getCenterX() );
		connector.setEndY( bounds.getMinY() );
	}

	public CubicCurve getConnector() {
		return connector;
	}

}
