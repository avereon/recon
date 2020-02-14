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

		double spaceX = 100;
		double spaceY = 100;
		int row = 0;
		int column;
		for( Map<String, List<NetworkDevice>> level : levels ) {
			column = 0;
			for( String key : level.keySet() ) {
				for( NetworkDevice device : level.get( key ) ) {
					NetworkDeviceView view = views.get( device );
					if( view == null ) continue;

					double x = column * spaceX;
					double y = getHeight() - (row * spaceY);
					view.relocate( x, y );
					column++;
				}
			}
			row++;
		}
	}

	void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		getChildren().clear();
		views.clear();

		graph.getRootDevice().walk( this::registerDevice );
		graph.register( NodeEvent.CHILD_ADDED, e -> registerDevice( (NetworkDevice)e.getNewValue() ) );
		graph.register( NodeEvent.CHILD_REMOVED, e -> unregisterDevice( (NetworkDevice)e.getOldValue() ) );

		requestLayout();
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

			return view;
		} );
	}

	private void unregisterDevice( NetworkDevice device ) {
		views.computeIfPresent( device, ( k, v ) -> {
			Map<String, List<NetworkDevice>> level = levels.get( device.getLevel() );
			List<NetworkDevice> group = level.get( device.getGroup() );
			group.remove( device );
			getChildren().remove( v );
			return null;
		} );
	}

}
