package com.avereon.recon;

import com.avereon.data.NodeComparator;
import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.CubicCurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkGraphTree extends Pane {

	private static final System.Logger log = Log.get();

	private static final double DEVICE_HORIZONTAL_SPACING = 3 * NetworkDeviceView.EXPECTED_STATE_SIZE;

	private static final double DEVICE_VERTICAL_SPACING = 5 * NetworkDeviceView.EXPECTED_STATE_SIZE;

	private static final double GROUP_PADDING = NetworkDeviceView.EXPECTED_STATE_SIZE;

	private static final Paint CONNECTOR_PAINT = Color.GRAY;

	private NetworkGraph graph;

	private List<Map<String, List<NetworkDevice>>> levels;

	private Map<String, Pane> groupViews;

	private Map<NetworkDevice, NetworkDeviceView> views;

	public NetworkGraphTree() {
		levels = new CopyOnWriteArrayList<>();
		groupViews = new ConcurrentHashMap<>();
		views = new ConcurrentHashMap<>();
	}

	@Override
	protected void layoutChildren() {
		if( graph == null ) return;

		for( Node child : getChildren() ) {
			if( child.isResizable() && child.isManaged() ) child.autosize();
		}

		int level = 0;
		double nextX;
		double nextY = 0.5 * DEVICE_VERTICAL_SPACING;
		for( Map<String, List<NetworkDevice>> groups : levels ) {
			// Levels
			nextX = 0.5 * (getWidth() - getLevelWidth( groups ));
			List<String> groupNames = new ArrayList<>( groups.keySet() );
			Collections.sort( groupNames );
			for( String group : groupNames ) {
				// Groups
				nextX += GROUP_PADDING;
				boolean firstInGroup = true;
				Pane groupView = groupViews.get( level + "-" + group );
				List<NetworkDevice> devices = new ArrayList<>( groups.get( group ) );
				devices.sort( new NodeComparator<>( List.of( "type", "name" ) ) );

				double minX = Double.MAX_VALUE;
				double maxX = Double.MIN_VALUE;
				for( NetworkDevice device : devices ) {
					// Devices
					NetworkDeviceView view = views.get( device );
					if( view == null ) {
						log.log( Log.WARN, "Missing view for: " + device.getName() );
						continue;
					}

					double x = nextX;
					double y = getHeight() - 0.5 * view.getHeight() - nextY;
					view.relocate( x, y );

					minX = Math.min( minX, x );
					maxX = Math.max( maxX, x + view.getWidth() );
					if( firstInGroup ) {
						double w = Math.max( view.getWidth(), groupView.getWidth() );
						double h = Math.max( view.getHeight(), groupView.getPrefHeight() );
						double offset = groupView.getPrefHeight();
						groupView.resizeRelocate( x, y - offset, w, h + offset );
					} else {
						double w = view.getLayoutX() + view.getWidth() - groupView.getLayoutX();
						double h = view.getLayoutY() + view.getHeight() - groupView.getLayoutY();
						groupView.resize( w, h );
					}

					nextX += DEVICE_HORIZONTAL_SPACING;
					firstInGroup = false;
				}

				// In the rare case the group width is larger than all the devices
				// This can happen with long names and few devices
				double delta = maxX - minX;
				if(delta < groupView.getWidth() ) {
					double adjust = 0.5 * (groupView.getWidth() - delta);
					groupView.relocate( groupView.getLayoutX() - adjust, groupView.getLayoutY() );
				}

				nextX += GROUP_PADDING;
			}
			nextY += DEVICE_VERTICAL_SPACING;
			level++;
		}
	}

	private double getLevelWidth( Map<String, List<NetworkDevice>> level ) {
		double width = 0;
		for( List<NetworkDevice> group : level.values() ) {
			width += getGroupWidth( group );
		}
		return width;
	}

	private double getGroupWidth( List<NetworkDevice> group ) {
		double width = GROUP_PADDING;
		width += group.size() * DEVICE_HORIZONTAL_SPACING;
		width += GROUP_PADDING;
		return width;
	}

	void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		getChildren().clear();
		views.clear();

		graph.getRootDevice().walk( this::registerDevice );
		graph.register( NodeEvent.CHILD_ADDED, e -> addDevice( (NetworkDevice)e.getNewValue() ) );
		// TODO if NodeEvent.REMOVING_CHILD is supported change the event handler
		graph.register( NodeEvent.CHILD_REMOVED, e -> removeDevice( (NetworkDevice)e.getNode(), (NetworkDevice)e.getOldValue() ) );
		graph.register( NodeEvent.NODE_CHANGED, e -> requestLayout() );

		requestLayout();
	}

	private void addDevice( NetworkDevice device ) {
		registerDevice( device );
		views.get( device ).getDetails().setVisible( true );
	}

	private void removeDevice( NetworkDevice parent, NetworkDevice device ) {
		unregisterDevice( parent, device );
	}

	private void registerDevice( NetworkDevice device ) {
		views.computeIfAbsent( device, d -> {
			List<Node> nodes = new ArrayList<>();
			NetworkDeviceView view = new NetworkDeviceView( d );
			nodes.add( view );
			nodes.add( view.getDetails() );

			int level = device.getLevel();
			if( level >= levels.size() ) {
				for( int index = levels.size(); index <= level; index++ ) {
					levels.add( new ConcurrentHashMap<>() );
				}
			}
			Map<String, List<NetworkDevice>> group = levels.get( level );
			List<NetworkDevice> list = group.computeIfAbsent( device.getGroup(), name -> {
				NetworkGroupView groupView = new NetworkGroupView( name );
				groupView.setViewOrder( 1 );
				nodes.add( groupView );
				groupViews.put( level + "-" + name, groupView );
				return new CopyOnWriteArrayList<>();
			} );
			list.add( device );

			// Add a line from this device to the parent
			if( !device.isRoot() ) {
				NetworkDevice parent = device.getParent();
				NetworkDeviceView parentView = views.get( parent );
				if( parentView == null ) log.log( Log.WARN, "Parent view is null" );

				double offset = 0.8 * DEVICE_VERTICAL_SPACING;
				CubicCurve curve = new CubicCurve();
				device.setConnector( curve );
				curve.getStyleClass().addAll( "network-device-connector" );
				curve.setViewOrder( 2 );
				curve.startXProperty().bind( view.layoutXProperty().add( view.widthProperty().multiply( 0.5 ) ) );
				curve.startYProperty().bind( view.layoutYProperty().add( view.heightProperty().multiply( 0.5 ) ) );
				curve.controlX1Property().bind( view.layoutXProperty().add( view.widthProperty().multiply( 0.5 ) ) );
				curve.controlY1Property().bind( view.layoutYProperty().add( view.heightProperty().multiply( 0.5 ) ).add( offset ) );
				curve.controlX2Property().bind( parentView.layoutXProperty().add( parentView.widthProperty().multiply( 0.5 ) ) );
				curve.controlY2Property().bind( parentView.layoutYProperty().add( parentView.heightProperty().multiply( 0.5 ) ).subtract( offset ) );
				curve.endXProperty().bind( parentView.layoutXProperty().add( parentView.widthProperty().multiply( 0.5 ) ) );
				curve.endYProperty().bind( parentView.layoutYProperty().add( parentView.heightProperty().multiply( 0.5 ) ) );
				curve.setStroke( CONNECTOR_PAINT );
				curve.setFill( null );
				nodes.add( curve );
			}
			getChildren().addAll( nodes );

			return view;
		} );
	}

	private void unregisterDevice( NetworkDevice parent, NetworkDevice device ) {
		views.computeIfPresent( device, ( k, v ) -> {
			Map<String, List<NetworkDevice>> level = levels.get( parent.getLevel() + 1 );
			level.get( device.getGroup() ).remove( device );

			getChildren().removeAll( v, v.getDetails(), device.getConnector() );
			return null;
		} );
	}

}
