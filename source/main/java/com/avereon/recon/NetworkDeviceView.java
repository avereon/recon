package com.avereon.recon;

import com.avereon.data.NodeEvent;
import com.avereon.util.Log;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

class NetworkDeviceView extends HBox {

	private static final System.Logger log = Log.log();

	private static final double EXPECTED_STATE_SIZE = 25;

	private static final double STATE_SIZE = 20;

	private NetworkDevice device;

	private Shape expected;

	private Shape currentState;

	private VBox details;

	private TextField name;

	private TextField host;

	private Label address;

	public NetworkDeviceView( NetworkDevice device ) {
		this.device = device;

		getStyleClass().addAll( "network-device" );

		currentState = new Circle( STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		expected = new Circle( EXPECTED_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		StackPane state = new StackPane( expected, currentState );

		name = new TextField( device.getName() );
		name.setAlignment( Pos.CENTER );
		//name.setPrefWidth( EXPECTED_STATE_SIZE );

		host = new TextField( device.getHost() );
		host.setAlignment( Pos.CENTER );
		//host.setPrefWidth( EXPECTED_STATE_SIZE );

		address = new Label( device.getAddress() );
		address.setAlignment( Pos.CENTER );
		//address.setPrefWidth( EXPECTED_STATE_SIZE );

		details = new VBox();
		details.setAlignment( Pos.CENTER );
		details.getChildren().addAll( name, host, address );
		//details.setVisible( false );
		//details.setManaged( false );
		//details.requestLayout();
		//details.layoutXProperty().bindBidirectional( expected.layoutXProperty() );

		currentState.layoutBoundsProperty().addListener( ( p, o, n ) -> {
			details.relocate( n.getMaxX(), n.getMaxY() );
		} );

		//setAlignment( Pos.CENTER );
		getChildren().addAll( state, details );

		device.register( NodeEvent.ANY, e -> Platform.runLater( this::updateState ) );

		currentState.setFocusTraversable( true );
		currentState.addEventHandler( MouseEvent.MOUSE_PRESSED, e -> currentState.requestFocus() );
		currentState.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			if( e.getCode() == KeyCode.EQUALS ) {
				getDevice().addDevice( new NetworkDevice().setName( "New Device" ).setHost( "unknown" ) );
			} else if( e.getCode() == KeyCode.MINUS ) {
				com.avereon.data.Node parent = getDevice().getParent();
				if( parent != null && !(parent instanceof NetworkGraph) ) ((NetworkDevice)parent).removeDevice( device );
			}
		} );

		new FieldInputHandler( currentState, name, () -> device.setName( name.getText() ) );
		new FieldInputHandler( currentState, host, () -> device.setHost( host.getText() ) );

		updateState();
	}

	NetworkDevice getDevice() {
		return device;
	}

	private void updateState() {
		expected.setFill( getDevice().getExpected().getPaint() );
		currentState.setFill( getDevice().getResponse().getPaint() );
		name.setText( getDevice().getName() );
		host.setText( getDevice().getHost() );
		address.setText( getDevice().getAddress() );
		//details.setVisible( getDevice().getResponse() != DeviceResponse.UNKNOWN && getDevice().getExpected() != getDevice().getResponse() );
	}

	private static class FieldInputHandler {

		private String priorValue;

		private FieldInputHandler( Node node, TextField field, Runnable action ) {
			field.addEventHandler( MouseEvent.MOUSE_CLICKED, e -> {
				priorValue = field.getText();
			} );
			field.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
				if( e.getCode() == KeyCode.ESCAPE ) {
					field.setText( priorValue );
					blur( node );
				}
				if( e.getCode() == KeyCode.ENTER ) {
					action.run();
					blur( node );
				}
			} );
			field.focusedProperty().addListener( ( p, o, n ) -> {
				if( !n ) {
					action.run();
					blur( node );
				}
			} );
		}

		private void blur( Node node ) {
			node.requestFocus();
		}

	}

}
