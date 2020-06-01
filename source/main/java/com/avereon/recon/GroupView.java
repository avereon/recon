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
import javafx.scene.shape.Shape;

public class GroupView extends BorderPane {

	private final String key;

	private final StackPane container;

	private Orientation orientation;

	private Pane box;

	private Shape connector;

	public GroupView( String key, String group ) {
		getStyleClass().add( "group-view" );

		this.key = key;
		this.container = new StackPane();
		this.orientation = Orientation.VERTICAL;
		this.container.getChildren().add( box = new VBox() );

		Label label = new Label( group );
		BorderPane.setAlignment( label, Pos.CENTER );
		setTop( label );
		setCenter( container );
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
		} else {
			box = new VBox();
		}
		box.getChildren().addAll( oldBox.getChildren() );
		container.getChildren().clear();
		container.getChildren().add( box );

		this.orientation = orientation;
		requestLayout();
	}

	public void linkToParent( GroupView sourceView ) {
		double offset = 40;

		CubicCurve curve = new CubicCurve();
		curve.getStyleClass().addAll( "group-connector" );
		curve.setManaged( false );
		curve.setViewOrder( 2 );
		curve.setFill( null );

		// The source view is in a different level view so it's a bit more complicated
		sourceView.localToSceneTransformProperty().addListener( ( v, o, n ) -> {
			Bounds sourceBounds = sourceView.getLayoutBounds();
			Point2D sourceAnchor = curve.sceneToLocal( n.transform( sourceBounds.getCenterX(), sourceBounds.getMaxY() ) );
			curve.setStartX( sourceAnchor.getX() );
			curve.setStartY( sourceAnchor.getY() );
			curve.setControlX1( sourceAnchor.getX() );
			curve.setControlY1( sourceAnchor.getY() + offset );
		} );

		GroupView targetView = this;
		targetView.boundsInParentProperty().addListener( (v,o,n) -> {
			curve.setControlX2( n.getCenterX() );
			curve.setControlY2( n.getMinY() - offset );
			curve.setEndX( n.getCenterX() );
			curve.setEndY( n.getMinY() );
		});

		targetView.setConnector( curve );
	}

	public Shape getConnector() {
		return connector;
	}

	public void setConnector( Shape connector ) {
		this.connector = connector;
	}

}
