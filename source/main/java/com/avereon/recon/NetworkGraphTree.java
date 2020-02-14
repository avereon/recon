package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkGraphTree extends Pane {

	private static final System.Logger log = Log.log();

	private static final double DEVICE_HORIZONTAL_SPACING = 3 * NetworkDeviceView.EXPECTED_STATE_SIZE;

	private static final double DEVICE_VERTICAL_SPACING = 5 * NetworkDeviceView.EXPECTED_STATE_SIZE;

	private static final double GROUP_PADDING = NetworkDeviceView.EXPECTED_STATE_SIZE;

	private NetworkGraph graph;

	private Map<NetworkDevice, NetworkDeviceView> views;

	private List<Map<String, List<NetworkDevice>>> levels;

	public NetworkGraphTree() {
		views = new ConcurrentHashMap<>();
		levels = new CopyOnWriteArrayList<>();
	}

	@Override
	protected void layoutChildren() {
		if( graph == null ) return;

		for( Node child : getChildren() ) {
			if( child.isResizable() && child.isManaged() ) child.autosize();
		}

		double nextX;
		double nextY = 0.5 * DEVICE_VERTICAL_SPACING;
		for( Map<String, List<NetworkDevice>> level : levels ) {
			// Levels
			nextX = 0.5 * (getWidth() - getLevelWidth( level ));
			for( String group : level.keySet() ) {
				// Groups
				nextX += GROUP_PADDING;
				for( NetworkDevice device : level.get( group ) ) {
					// Devices
					NetworkDeviceView view = views.get( device );
					if( view == null ) continue;

					view.relocate( nextX, getHeight() - 0.5 * view.getHeight() - nextY );
					nextX += DEVICE_HORIZONTAL_SPACING;
				}
				nextX += GROUP_PADDING;
			}
			nextY += DEVICE_VERTICAL_SPACING;
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
			NetworkDeviceView view = new NetworkDeviceView( d );
			getChildren().addAll( view, view.getDetails() );

			int level = device.getLevel();
			if( level >= levels.size() ) {
				for( int index = levels.size(); index <= level; index++ ) {
					levels.add( new ConcurrentHashMap<>() );
				}
			}
			Map<String, List<NetworkDevice>> map = levels.get( level );
			List<NetworkDevice> list = map.computeIfAbsent( device.getGroup(), k -> new CopyOnWriteArrayList<>() );
			list.add( device );

			// Add a line from this device to the parent
			if( !device.isRoot() ) {
				NetworkDeviceView parentView = views.get( device.getParent() );

				Line line = new Line();
				line.setViewOrder( 1 );
				line.startXProperty().bind( view.layoutXProperty().add( view.widthProperty().multiply( 0.5 ) ) );
				line.startYProperty().bind( view.layoutYProperty().add( view.heightProperty().multiply( 0.5 ) ) );
				line.endXProperty().bind( parentView.layoutXProperty().add( parentView.widthProperty().multiply( 0.5 ) ) );
				line.endYProperty().bind( parentView.layoutYProperty().add( parentView.heightProperty().multiply( 0.5 ) ) );
				getChildren().add( line );

				device.putResource( NetworkDevice.CONNECTOR, line );
			}

			return view;
		} );
	}

	private void unregisterDevice( NetworkDevice parent, NetworkDevice device ) {
		views.computeIfPresent( device, ( k, v ) -> {
			Map<String, List<NetworkDevice>> level = levels.get( parent.getLevel() + 1 );
			level.get( device.getGroup() ).remove( device );

			getChildren().removeAll( v, v.getDetails(), device.getResource( NetworkDevice.CONNECTOR ) );
			return null;
		} );
	}

}
