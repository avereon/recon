package com.avereon.recon;

import com.avereon.util.Log;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkGraphTree extends VBox {

	private static final System.Logger log = Log.get();

	private final Map<Integer, LevelView> levels;

	private NetworkGraph graph;

	public NetworkGraphTree() {
		levels = new ConcurrentHashMap<>();
	}

	void setNetworkGraph( NetworkGraph graph ) {
		if( this.graph == graph ) return;
		this.graph = graph;

		getChildren().clear();
		levels.clear();

		graph.getRootDevice().walk( this::addDevice );
	}

	private static String getGroupKey( NetworkDevice device ) {
		return (device.isRoot() ? "root" : ((NetworkDevice)device.getParent()).getId()) + "-" + device.getGroup();
	}

	private void addDevice( NetworkDevice device ) {
		LevelView levelView = getLevelView( device.getLevel() );
		GroupView groupView = levelView.getGroupView( device );
		DeviceView deviceView = new DeviceView( device );
		groupView.getViews().add( deviceView );
		device.setGroupView( groupView );
		getChildren().add( deviceView.getDetails() );

		deviceView.getDetails().requestLayout();
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		getChildren().stream().filter( n -> n instanceof DeviceDetailView ).forEach( Node::autosize );
	}

	private void removeDevice( NetworkDevice device ) {
		LevelView levelView = getLevelView( device.getLevel() );
		GroupView groupView = levelView.getGroupView( device );
		Optional<Node> deviceViewOptional = groupView.getViews().stream().filter( c -> ((DeviceView)c).getDevice() == device ).findAny();
		if( deviceViewOptional.isPresent() ) {
			DeviceView deviceView = (DeviceView)deviceViewOptional.get();
			groupView.getViews().remove( deviceView );
			getChildren().remove( deviceView.getDetails() );

			// If the last child in the group, remove the group
			if( groupView.getChildren().size() == 0 ) levelView.removeGroupView( groupView );
		}
	}

	private LevelView getLevelView( int level ) {
		return levels.computeIfAbsent( level, l -> {
			//log.log( Log.WARN, "Adding level=" + l );
			LevelView view = new LevelView( l );
			view.setAlignment( Pos.CENTER );
			getChildren().add( view );
			return view;
		} );
	}

	private static class LevelView extends TilePane {

		private final int level;

		private final Map<String, GroupView> groups;

		public LevelView( int level ) {
			getStyleClass().add( "level-view" );
			this.level = level;
			this.groups = new ConcurrentHashMap<>();
		}

		public int getLevel() {
			return level;
		}

		public GroupView getGroupView( NetworkDevice device ) {
			String groupKey = getGroupKey( device );
			return groups.computeIfAbsent( groupKey, k -> {
				GroupView view = new GroupView( k, device.getGroup() );
				TilePane.setAlignment( view, Pos.CENTER );
				view.updateOrientation();
				getChildren().add( view );
				if( !device.isRoot() ) {
					view.linkToParent( ((NetworkDevice)device.getParent()).getGroupView() );
					getChildren().add( view.getConnector() );
				}
				return view;
			} );
		}

		public void removeGroupView( GroupView view ) {
			getChildren().remove( view );
			getChildren().remove( view.getConnector() );
		}

	}

}
