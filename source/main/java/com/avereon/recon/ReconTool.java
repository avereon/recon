package com.avereon.recon;

import com.avereon.util.Log;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ProgramTool;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.task.TaskManager;
import com.avereon.xenon.util.Lambda;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ReconTool extends ProgramTool {

	private static final System.Logger log = Log.log();

	private static Map<DeviceResponse, Paint> deviceResponsePaint;

	private static Timer timer = new Timer( true );

	private NetworkGraphView networkGraphView;

	private RunPauseAction runPauseAction;

	private TimerTask updateTask;

	static {
		deviceResponsePaint = new HashMap<>();
		deviceResponsePaint.put( DeviceResponse.UNKNOWN, Color.RED.darker() );
		deviceResponsePaint.put( DeviceResponse.OFFLINE, Color.RED );
		deviceResponsePaint.put( DeviceResponse.ONLINE, Color.GREEN );
		deviceResponsePaint.put( DeviceResponse.OFF, Color.GRAY );
	}

	public ReconTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "recon" ) );

		runPauseAction = new RunPauseAction( getProgram() );

		getChildren().addAll( networkGraphView = new NetworkGraphView() );
	}

	@Override
	protected void assetReady( OpenAssetRequest request ) throws ToolException {
		networkGraphView.getChildren().add( new NetworkDeviceNode( getGraph().getRootDevice() ) );
	}

	@Override
	protected void activate() throws ToolException {
		pushAction( "runpause", runPauseAction );
		getProgram().getWorkspaceManager().getActiveWorkspace().pushToolbarActions( "runpause" );
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
			setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
		}

	}

	private void requestUpdates() {
		TaskManager taskManager = getProgram().getTaskManager();
		getGraph().getRootDevice().walk( e -> taskManager.submit( Task.of( e.getName(), e::updateStatus ) ) );
	}

	private class NetworkDeviceNode extends VBox {

		private NetworkDevice device;

		private Shape shape;

		private Label name;

		private Label address;

		public NetworkDeviceNode( NetworkDevice device ) {
			this.device = device;

			this.shape = new Circle( 30, Color.BLACK );
			this.name = new Label( device.getName() );
			this.address = new Label( device.getAddress() );

			setAlignment( Pos.CENTER );
			getChildren().addAll( shape, name, address );

			device.addNodeListener( e -> Platform.runLater( this::updateState ) );

			updateState();
		}

		private void updateState() {
			DeviceRequest request = getDevice().getRequest();
			DeviceResponse response = getDevice().getResponse();

			Paint paint = Color.BLUE;
			if( request == DeviceRequest.RUNNING ) paint = deviceResponsePaint.get( response );

			shape.setFill( paint );
		}

		NetworkDevice getDevice() {
			return device;
		}

	}

	private class RunPauseAction extends Action {

		protected RunPauseAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public void handle( ActionEvent event ) {
			synchronized( this ) {
				if( updateTask != null ) {
					updateTask.cancel();
					updateTask = null;
				} else {
					timer.schedule( updateTask = Lambda.timerTask( ReconTool.this::requestUpdates ), 0, 1000 );
				}
			}
		}

	}

}
