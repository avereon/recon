package com.avereon.recon;

import com.avereon.util.Log;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Shape;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkGraphTree2 extends StackPane {

	private static final System.Logger log = Log.get();

	private static final Paint CONNECTOR_PAINT = Color.GRAY;

	private final Map<Integer, LevelView> levels;

	private NetworkGraph graph;

	private final Pane connectorPane;

	private final VBox viewPane;

	public NetworkGraphTree2() {
		levels = new ConcurrentHashMap<>();
		setStyle( "-fx-background-color: #0000ff40" );

		getChildren().add( connectorPane = new Pane() );
		getChildren().add( viewPane = new VBox() );
	}

	void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		viewPane.getChildren().clear();
		levels.clear();

		graph.getRootDevice().walk( this::addDevice );
	}

	private static String getGroupKey( NetworkDevice device ) {
		return (device.isRoot() ? "root" : ((NetworkDevice)device.getParent()).getId()) + "-" + device.getGroup();
	}

	private void addDevice( NetworkDevice device ) {
		LevelView level = getLevelView( device.getLevel() );
		GroupView group = level.getGroupView( device );
		group.getViews().add( new NetworkDeviceView( device ) );
		device.setGroupView( group );
	}

	private void removeDevice( NetworkDevice device ) {
		LevelView level = getLevelView( device.getLevel() );
		GroupView group = level.getGroupView( device );
		group.getViews().removeIf( c -> ((NetworkDeviceView)c).getDevice() == device );
	}

	private LevelView getLevelView( int level ) {
		return levels.computeIfAbsent( level, l -> {
			//log.log( Log.WARN, "Adding level=" + l );
			LevelView view = new LevelView( l );
			view.setAlignment( Pos.CENTER );
			viewPane.getChildren().add( view );
			return view;
		} );
	}

	private class LevelView extends TilePane {

		private final int level;

		private final Map<String, GroupView> groups;

		public LevelView( int level ) {
			this.level = level;
			this.groups = new ConcurrentHashMap<>();
			setStyle( "-fx-padding: 0.5cm 0 0.5cm 0" );
		}

		public int getLevel() {
			return level;
		}

		public GroupView getGroupView( NetworkDevice device ) {
			String groupKey = getGroupKey( device );
			return groups.computeIfAbsent( groupKey, k -> {
				//log.log( Log.WARN, "Adding group=" + k );
				GroupView view = new GroupView( k, device.getGroup() );
				TilePane.setAlignment( view, Pos.CENTER );
				getChildren().add( view );
				view.updateOrientation();

				if( !device.isRoot() ) linkGroups( ((NetworkDevice)device.getParent()).getGroupView(), view );

				return view;
			} );
		}

		private void linkGroups( Region parentView, Region childView ) {
			double offset = 0;

			CubicCurve curve = new CubicCurve();
			//curve.getStyleClass().addAll( "network-device-connector" );
			curve.setViewOrder( 2 );

			curve.startXProperty().bind( parentView.layoutXProperty().add( parentView.widthProperty().multiply( 0.5 ) ) );
			curve.startYProperty().bind( parentView.layoutYProperty().add( parentView.heightProperty() ) );

			curve.controlX1Property().bind( parentView.layoutXProperty().add( parentView.widthProperty().multiply( 0.5 ) ) );
			curve.controlY1Property().bind( parentView.layoutYProperty().add( parentView.heightProperty() ).add( offset ) );

			curve.controlX2Property().bind( childView.layoutXProperty().add( childView.widthProperty().multiply( 0.5 ) ) );
			curve.controlY2Property().bind( childView.layoutYProperty().subtract( offset ) );

			curve.endXProperty().bind( childView.layoutXProperty().add( childView.widthProperty().multiply( 0.5 ) ) );
			curve.endYProperty().bind( childView.layoutYProperty() );

			curve.setStroke( CONNECTOR_PAINT );
			curve.setFill( null );

			connectorPane.getChildren().add( curve );
		}

	}

	private static class GroupView extends BorderPane {

		private final String key;

		private final StackPane container;

		private Orientation orientation;

		private Pane box;

		private Shape leader;

		public GroupView( String key, String group ) {
			this.key = key;
			this.container = new StackPane();
			this.orientation = Orientation.VERTICAL;
			this.container.getChildren().add( box = new VBox() );

			setStyle( "-fx-background-color: #ffff0040" );
			//setMaxWidth( Double.MAX_VALUE );
			//setPrefWidth( Double.MAX_VALUE );

			Label label = new Label( group );
			BorderPane.setAlignment( label, Pos.CENTER );
			//label.setAlignment( Pos.CENTER );
			label.setStyle( "-fx-background-color: #80808080" );
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
			long count = getViews().stream().mapToInt( n -> ((NetworkDeviceView)n).getDevice().getDevices().size() ).count();
			setOrientation( count > 0 ? Orientation.HORIZONTAL : Orientation.VERTICAL );
		}

		public void setOrientation( Orientation orientation ) {
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
		}

	}

}
