package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class NetworkGraphView extends VBox {

	private static final System.Logger log = Log.get();

	private NetworkGraph graph;

	public NetworkGraphView() {
		setAlignment( Pos.CENTER );
	}

	public void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		if( getChildren().size() == 0 ) addToLevel( 0, List.of( graph.getRootDevice() ) );

		graph.register( NodeEvent.CHILD_ADDED, e -> {
			int level = e.getNode().distanceTo( graph.getRootDevice() ) + 1;
			addToLevel( level, List.of( (NetworkDevice)e.getNewValue() ) );
		} );

		graph.register( NodeEvent.CHILD_REMOVED, e -> {
			int level = e.getNode().distanceTo( graph.getRootDevice() ) + 1;
			removeFromLevel( level, List.of( (NetworkDevice)e.getOldValue() ) );
		} );
	}

	private List<NetworkDevice> getChildDevices( Collection<NetworkDevice> devices ) {
		return devices.stream().flatMap( d -> d.getDevices().stream() ).collect( Collectors.toList() );
	}

	private void addToLevel( int level, Collection<NetworkDevice> devices ) {
		HBox row = calcLevelRowIfAbsent( level );
		devices.forEach( d -> addToRow( row, d ) );
		Collection<NetworkDevice> childDevices = getChildDevices( devices );
		if( childDevices.size() > 0 ) addToLevel( level + 1, childDevices );
	}

	private HBox calcLevelRowIfAbsent( int level ) {
		HBox row = getLevelRow( level );
		if( row == null ) {
			row = new HBox();
			getChildren().add( 0, row );
			row.setAlignment( Pos.CENTER );
		}
		return row;
	}

	private HBox getLevelRow( int level ) {
		int index = (getChildren().size() - 1) - level;
		boolean valid = index >= 0 && index < getChildren().size();
		return !valid ? null : (HBox)getChildren().get( index );
	}

	private void addToRow( HBox row, NetworkDevice device ) {
		if( row.getChildren().stream().anyMatch( c -> ((NetworkDeviceView)c).getDevice().equals( device ) ) ) return;
		row.getChildren().add( new NetworkDeviceView( device ) );
	}

	private void removeFromLevel( int level, Collection<NetworkDevice> devices ) {
		HBox row = getLevelRow( level );
		if( row == null ) return;
		row
			.getChildren()
			.removeAll( row.getChildren().stream().filter( c -> devices.contains( ((NetworkDeviceView)c).getDevice() ) ).collect( Collectors.toSet() ) );
		removeFromLevel( level + 1, getChildDevices( devices ) );
	}

}
