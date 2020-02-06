package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ReconTool extends ProgramTool {

	private static final System.Logger log = Log.log();

	private static Timer timer = new Timer( true );

	private NetworkGraphView networkGraphView;

	private RunPauseAction runPauseAction;

	private TimerTask updateTask;

	private int updateInterval = 5000;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "recon" ) );

		runPauseAction = new RunPauseAction( this );

		getChildren().addAll( networkGraphView = new NetworkGraphView() );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		// Collect all the network devices

		int level = 0;
		List<NetworkDevice> devices = List.of( getGraph().getRootDevice() );

		while( devices.size() > 0 ) {
			networkGraphView.getChildren().add( 0, buildRow( level, devices ) );
			devices = getDeviceList( devices );
			level++;
		}

	}

	synchronized void start() {
		if( isRunning() ) return;
		timer.schedule( updateTask = Lambda.timerTask( ReconTool.this::requestUpdates ), 0, updateInterval );
	}

	boolean isRunning() {
		return updateTask != null;
	}

	synchronized void stop() {
		if( updateTask != null ) updateTask.cancel();
		updateTask = null;
	}

	private List<NetworkDevice> getDeviceList( Collection<NetworkDevice> devices ) {
		return devices.stream().flatMap( d -> d.getDevices().stream() ).collect( Collectors.toList() );
	}

	private HBox buildRow( int level, Collection<NetworkDevice> devices ) {
		HBox row = new HBox();
		row.setAlignment( Pos.CENTER );
		row.getChildren().addAll( devices.stream().map( NetworkDeviceNode::new ).collect( Collectors.toList() ) );
		return row;
	}

	@Override
	protected void activate() throws ToolException {
		pushAction( "runpause", runPauseAction );
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( "runpause" );

		Platform.runLater( () -> getProgram().getActionLibrary().getAction( "runpause" ).setState( updateTask == null ? "run" : "pause" ) );
	}

	@Override
	protected void conceal() throws ToolException {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullToolbarActions();
		pullAction( "runpause", runPauseAction );
	}

	private NetworkGraph getGraph() {
		return getAsset().getModel();
	}

	private class NetworkGraphView extends VBox {

		public NetworkGraphView() {
			setAlignment( Pos.CENTER );
		}

	}

	private void requestUpdates() {
		TaskManager taskManager = getProgram().getTaskManager();
		getGraph().getRootDevice().walk( e -> taskManager.submit( Task.of( e.getName(), () -> e.updateStatus() ) ) );
	}

}
