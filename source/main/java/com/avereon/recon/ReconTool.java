package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.event.EventHandler;
import com.avereon.util.Log;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReconTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private static final Timer timer = new Timer( true );

	private final NetworkGraphTree networkGraphTree;

	private final RunPauseAction runPauseAction;

	private TimerTask updateTask;

	private int updateInterval = 20000;

	private int retryCount = 3;

	private int retryInterval = 2;

	private TimeUnit retryUnit = TimeUnit.SECONDS;

	private final EventHandler<NodeEvent> modelChangeHandler;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		getStylesheets().add( Objects.requireNonNull( product.getClassLoader().getResource( "recon.css" ) ).toExternalForm() );

		runPauseAction = new RunPauseAction( this );

		// NOTE Adding the scroller really messes with the focus handling and key events
		ScrollPane scroller = new ScrollPane( networkGraphTree = new NetworkGraphTree() );
		scroller.setFitToHeight( true );
		scroller.setFitToWidth( true );

		getChildren().addAll( networkGraphTree );

		modelChangeHandler = e -> networkGraphTree.setNetworkGraph( getGraph() );
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
	protected void ready( OpenAssetRequest request ) {
		setTitle( getAsset().getName() );
		setGraphic( getProgram().getIconLibrary().getIcon( "recon" ) );
		getGraph().register( NodeEvent.NODE_CHANGED, modelChangeHandler );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		networkGraphTree.setNetworkGraph( getGraph() );
	}

	@Override
	protected void activate() {
		pushAction( "runpause", runPauseAction );
		pushToolActions( "runpause" );

		Platform.runLater( () -> getProgram().getActionLibrary().getAction( "runpause" ).setState( isRunning() ? "pause" : "run" ) );
	}

	@Override
	protected void conceal() {
		pullToolActions();
		pullAction( "runpause", runPauseAction );
	}

	@Override
	protected void deallocate() {
		getGraph().unregister( NodeEvent.NODE_CHANGED, modelChangeHandler );
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
