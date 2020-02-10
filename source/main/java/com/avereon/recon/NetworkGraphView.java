package com.avereon.recon;

import com.avereon.data.NodeEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class NetworkGraphView extends VBox {

	private NetworkGraph graph;

	public NetworkGraphView(  ) {
		setAlignment( Pos.CENTER );
	}

	public void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		graph.register( NodeEvent.CHILD_ADDED, e -> {
			// An entire sub-tree could have been added starting at the child
		});

//		graph.addNodeListener( e -> {
//			switch( e.getEventType() ) {
//				case NodeEvent.CHILD_ADDED : {
//					break;
//				}
//			}
//		} );

//		int level = 0;
//		getChildren().clear();
//		List<NetworkDevice> devices = List.of( graph.getRootDevice() );
//		while( devices.size() > 0 ) {
//			getChildren().add( 0, buildRow( level, devices ) );
//			devices = getDeviceList( devices );
//			level++;
//		}
	}

	private List<NetworkDevice> getDeviceList( Collection<NetworkDevice> devices ) {
		return devices.stream().flatMap( d -> d.getDevices().stream() ).collect( Collectors.toList() );
	}

	private HBox buildRow( int level, Collection<NetworkDevice> devices ) {
		HBox row = new HBox();
		row.setAlignment( Pos.CENTER );
		row.getChildren().addAll( devices.stream().map( NetworkDeviceView::new ).collect( Collectors.toList() ) );
		return row;
	}

}
