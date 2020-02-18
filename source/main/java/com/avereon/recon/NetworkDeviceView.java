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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

class NetworkDeviceView extends StackPane {

	private static final System.Logger log = Log.log();

	static final double EXPECTED_STATE_SIZE = 10;

	private static final double ACTUAL_STATE_SIZE = 8;

	private NetworkDevice device;

	private Shape actual;

	private Shape expected;

	private VBox details;

	private TextField group;

	private TextField name;

	private TextField host;

	private Label address;

	public NetworkDeviceView( NetworkDevice device ) {
		this.device = device;

		getStyleClass().addAll( "network-device" );

		expected = new Circle( EXPECTED_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );
		actual = new Circle( ACTUAL_STATE_SIZE, DeviceResponse.UNKNOWN.getPaint() );

		group = new TextField( device.getGroup() );
		name = new TextField( device.getName() );
		host = new TextField( device.getHost() );
		address = new Label( device.getAddress() );

		details = new VBox();
		details.getStyleClass().add( "network-device-details" );
		details.setAlignment( Pos.CENTER_LEFT );
		details.getChildren().addAll( group, name, host, address );
		details.setVisible( false );
		details.setViewOrder( -1 );
		details.setFocusTraversable( true );
		details.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			if( e.getCode() == KeyCode.ESCAPE ) details.setVisible( false );
		} );

		setAlignment( Pos.CENTER );
		getChildren().addAll( expected, actual );

		device.register( NodeEvent.ANY, e -> Platform.runLater( this::updateState ) );

		actual.setMouseTransparent( true );

		expected.setFocusTraversable( true );
		expected.addEventHandler( MouseEvent.MOUSE_PRESSED, e -> {
			if( e.getClickCount() >= 2 ) {
				details.setVisible( !details.isVisible() );
				if( details.isVisible() ) details.requestFocus();
			} else {
				expected.requestFocus();
			}
		} );
		expected.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			//log.log( Log.WARN, e.getCode() + " pressed" );
			switch( e.getCode() ) {
				case INSERT: {
					getDevice().addDevice( new NetworkDevice().setName( "New Device" ).setHost( "unknown" ) );
					break;
				}
				case DELETE: {
					com.avereon.data.Node parent = getDevice().getParent();
					if( parent != null && !(parent instanceof NetworkGraph) ) ((NetworkDevice)parent).removeDevice( device );
					break;
				}
				case DIGIT0: {
					getDevice().setExpected( DeviceResponse.OFFLINE );
					break;
				}
				case DIGIT1: {
					getDevice().setExpected( DeviceResponse.ONLINE );
					break;
				}
			}
		} );

		new FieldInputHandler( group, () -> device.setGroup( group.getText() ) );
		new FieldInputHandler( name, () -> device.setName( name.getText() ) );
		new FieldInputHandler( host, () -> device.setHost( host.getText() ) );

		updateState();
	}

	@Override
	public void relocate( double x, double y ) {
		super.relocate( x, y );
		getDetails().relocate( getBoundsInParent().getMaxX(), getBoundsInParent().getCenterY() - getDetails().getBoundsInLocal().getCenterY() );
	}

	NetworkDevice getDevice() {
		return device;
	}

	Node getDetails() {
		return details;
	}

	private void updateState() {
		//details.setVisible( details.isVisible() || (getDevice().getResponse() != DeviceResponse.UNKNOWN && getDevice().getExpected() != getDevice().getResponse()) );
		group.setText( getDevice().getGroup() );
		name.setText( getDevice().getName() );
		host.setText( getDevice().getHost() );
		address.setText( getDevice().getAddress() );
		expected.setFill( getDevice().getExpected().getPaint() );
		actual.setFill( getDevice().getExpected().getPaint( getDevice().getResponse() ) );
	}

	private class FieldInputHandler {

		private String priorValue;

		private FieldInputHandler( TextField field, Runnable action ) {
			field.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
				if( e.getCode() == KeyCode.ESCAPE ) {
					field.setText( priorValue );
					details.requestFocus();
					e.consume();
				}
				if( e.getCode() == KeyCode.ENTER ) {
					action.run();
					details.requestFocus();
					e.consume();
				}
			} );
			field.focusedProperty().addListener( ( p, o, newFocusedValue ) -> {
				if( newFocusedValue ) priorValue = field.getText();
				if( !newFocusedValue ) action.run();
			} );
		}

	}

}
