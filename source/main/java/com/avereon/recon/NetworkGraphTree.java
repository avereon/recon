package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkGraphTree extends Pane {

	private static final System.Logger log = Log.log();

	private NetworkGraph graph;

	private Map<NetworkDevice, NetworkDeviceView> views;

	private List<Map<NetworkDeviceGroup, List<NetworkDevice>>> groups;

	public NetworkGraphTree() {
		views = new ConcurrentHashMap<>();
		groups = new CopyOnWriteArrayList<>();
	}

	@Override
	protected void layoutChildren() {
		if( graph == null ) return;

		for( Node child : getChildren() ) {
			if( child.isResizable() && child.isManaged() ) child.autosize();
		}

		// The root device
		NetworkDeviceView view = views.get( graph.getRootDevice() );
		if( view != null ) {
			double x = getBoundsInLocal().getCenterX() - view.getBoundsInLocal().getCenterX();
			double y = getBoundsInLocal().getCenterY() - view.getBoundsInLocal().getCenterY();
			view.relocate( x, y );
		}

		// NEXT Determine where to put the children
	}

	void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		getChildren().clear();
		views.clear();

		graph.getRootDevice().walk( this::registerDevice );
		graph.register( NodeEvent.CHILD_ADDED, e -> registerDevice( (NetworkDevice)e.getNewValue() ) );
		graph.register( NodeEvent.CHILD_REMOVED, e -> unregisterDevice( (NetworkDevice)e.getOldValue() ) );
	}

	private void registerDevice( NetworkDevice device ) {
		views.computeIfAbsent( device, d -> {
			NetworkDeviceView view = new NetworkDeviceView( d );
			getChildren().addAll( view, view.getDetails() );

			int level = device.getLevel();
			if( level >= groups.size() ) {
				for( int index = groups.size(); index <= level; index++ ) {
					groups.add( new ConcurrentHashMap<>() );
				}
			}
			// NEXT Keep working on device groups and graph layout
//			Map<NetworkDeviceGroup, List<NetworkDevice>> map = groups.get( level );
//			map.computeIfAbsent( device.getGroup(), k -> {
//				List<NetworkDevice> list = new CopyOnWriteArrayList<>();
//				list.add( device );
//				return list;
//			} );

			return view;
		} );
	}

	private void unregisterDevice( NetworkDevice device ) {
		views.computeIfPresent( device, ( k, v ) -> {
			getChildren().remove( v );
			return null;
		} );
	}

}
