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

import java.util.Timer;
import java.util.TimerTask;

public class ReconTool extends ProgramTool {

	private static final System.Logger log = Log.log();

	private static Timer timer = new Timer( true );

	private NetworkGraphView networkGraphView;

	private RunPauseAction runPauseAction;

	private TimerTask updateTask;

	private int updateInterval = 5000;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		getStylesheets().add( "recon.css" );

		setGraphic( product.getProgram().getIconLibrary().getIcon( "recon" ) );

		runPauseAction = new RunPauseAction( this );

		getChildren().addAll( networkGraphView = new NetworkGraphView() );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		networkGraphView.setNetworkGraph( getGraph() );
	}

	@Override
	protected void assetRefreshed() throws ToolException {
		networkGraphView.setNetworkGraph( getGraph() );
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

	private void requestUpdates() {
		TaskManager taskManager = getProgram().getTaskManager();
		getGraph().getRootDevice().walk( d -> taskManager.submit( Task.of( d.getName(), d::updateStatus ) ) );
	}

}
