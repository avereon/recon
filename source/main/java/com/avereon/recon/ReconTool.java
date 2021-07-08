package com.avereon.recon;

import com.avereon.product.Rb;
import com.avereon.skill.RunPauseResettable;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.action.common.RunPauseAction;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@CustomLog
public class ReconTool extends ProgramTool implements RunPauseResettable {

	private static final Timer timer = new Timer( true );

	private final NetworkGraphTree networkGraphTree;

	private final RunPauseAction runPauseAction;

	private TimerTask updateTask;

	private int updateInterval = 20000;

	private final int retryCount = 3;

	private final int retryInterval = 1;

	private final TimeUnit retryUnit = TimeUnit.SECONDS;

	//private final EventHandler<NodeEvent> modelChangeHandler;

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		addStylesheet( "recon.css" );

		runPauseAction = new RunPauseAction( getProgram(), this );

		// NOTE Adding the scroller really messes with the focus handling and key events
//		ScrollPane scroller = new ScrollPane( networkGraphTree = new NetworkGraphTree() );
//		scroller.setFitToWidth( true );
		getChildren().addAll( networkGraphTree = new NetworkGraphTree() );
//		networkGraphTree.setFocusTraversable( true );
//		scroller.setFocusTraversable( true );

		//modelChangeHandler = e -> networkGraphTree.setNetworkGraph( getGraph() );
	}

	@Override
	public synchronized void run() {
		if( isRunning() ) return;
		timer.schedule( updateTask = Lambda.timerTask( ReconTool.this::requestUpdates ), 0, updateInterval );
	}

	@Override
	public synchronized void pause() {
		if( isRunning() ) updateTask.cancel();
		updateTask = null;
	}

	@Override
	public void reset() {}

	boolean isRunning() {
		return updateTask != null;
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getAsset().getName() );
		setGraphic( getProgram().getIconLibrary().getIcon( "recon" ) );
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		networkGraphTree.setNetworkGraph( getGraph() );
		//getGraph().register( NodeEvent.NODE_CHANGED, modelChangeHandler );
	}

	@Override
	protected void activate() {
		pushAction( "runpause", runPauseAction );
		pushTools( "runpause" );
		if( getAsset().isLoaded() ) runPauseAction.setState( isRunning() ? "pause" : "run" );
	}

	@Override
	protected void conceal() {
		pullTools();
		pullAction( "runpause", runPauseAction );
	}

	@Override
	protected void deallocate() {
		//getGraph().unregister( NodeEvent.NODE_CHANGED, modelChangeHandler );
		pause();
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
		String label = Rb.text( BundleKey.LABEL, "update-network-device-status" );
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
					log.atError( exception ).log();
				}
			} );
		}
	}

}
