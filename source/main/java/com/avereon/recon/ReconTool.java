package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private static Timer timer = new Timer( true );

	private NetworkGraphTree networkGraphTree;

	private RunPauseAction runPauseAction;

	private TimerTask updateTask;

	private int updateInterval = 20000;

	private int retryCount = 3;

	private int retryInterval = 2;

	private TimeUnit retryUnit = TimeUnit.SECONDS;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		getStylesheets().add( product.getClassLoader().getResource( "recon.css" ).toExternalForm() );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "recon" ) );

		runPauseAction = new RunPauseAction( this );

		// NOTE Adding the scroller really messes with the focus handling and key events
		ScrollPane scroller = new ScrollPane( networkGraphTree = new NetworkGraphTree() );
		scroller.setFitToHeight( true );
		scroller.setFitToWidth( true );

		getChildren().addAll( networkGraphTree );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		networkGraphTree.setNetworkGraph( getGraph() );
	}

	@Override
	protected void assetRefreshed() throws ToolException {
		networkGraphTree.setNetworkGraph( getGraph() );
	}

	boolean isRunning() {
		return updateTask != null;
	}

	synchronized void start() {
		if( isRunning() ) return;
		timer.schedule( updateTask = Lambda.timerTask( ReconTool.this::requestUpdates ), 0, updateInterval );
	}

	synchronized void stop() {
		if( isRunning() ) updateTask.cancel();
		updateTask = null;
	}

	@Override
	protected void activate() throws ToolException {
		pushAction( "runpause", runPauseAction );
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( "runpause" );

		Platform.runLater( () -> getProgram().getActionLibrary().getAction( "runpause" ).setState( isRunning() ? "pause" : "run" ) );
	}

	@Override
	protected void conceal() throws ToolException {
		getProgram().getWorkspaceManager().getActiveWorkspace().pullToolbarActions();
		pullAction( "runpause", runPauseAction );
	}

	@Override
	protected void deallocate() throws ToolException {
		stop();
	}

	private NetworkGraph getGraph() {
		return getAsset().getModel();
	}

	private int getLevelCount() {
		final AtomicInteger level = new AtomicInteger( 0 );
		getGraph().getRootDevice().walk( d -> {
			int deviceLevel = d.getLevel();
			if( deviceLevel > level.get() ) level.set( deviceLevel );
		} );
		return level.get();
	}

	private Set<NetworkDevice> getDevicesInLevel( int level ) {
		Set<NetworkDevice> devices = new HashSet<>();
		getGraph().getRootDevice().walk( d -> {
			if( d.getLevel() == level ) devices.add( d );
		} );
		return devices;
	}

	private void requestUpdates() {
		String label = getProduct().rb().text( BundleKey.LABEL, "update-network-device-status" );
		int count = getLevelCount();
		TaskManager manager = getProgram().getTaskManager();
		for( int level = 0; level <= count; level++ ) {
			Set<NetworkDevice> devices = getDevicesInLevel( level );
			Set<Task<?>> tasks = new HashSet<>();
			devices.forEach( d -> tasks.add( manager.submit( Task.of( label, () -> d.updateStatus( retryCount, retryInterval, retryUnit ) ) ) ) );
			tasks.forEach( t -> {
				try {
					t.get();
				} catch( Exception exception ) {
					log.log( Log.ERROR, exception );
				}
			} );
		}

	}

}
