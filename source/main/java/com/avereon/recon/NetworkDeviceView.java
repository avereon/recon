package com.avereon.recon;

import com.avereon.data.NodeEvent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

class NetworkDeviceView extends VBox {

	private NetworkDevice device;

	private Shape shape;

	private TextField name;

	private TextField host;

	private Label address;

	public NetworkDeviceView( NetworkDevice device ) {
		this.device = device;

		getStyleClass().addAll( "network-device" );

		shape = new Circle( 20, DeviceResponse.UNKNOWN.getPaint() );

		name = new TextField( device.getName() );
		name.setAlignment( Pos.CENTER );

		host = new TextField( device.getHost() );
		host.setAlignment( Pos.CENTER );

		address = new Label( device.getAddress() );
		address.setAlignment( Pos.CENTER );

		setAlignment( Pos.CENTER );
		getChildren().addAll( shape, name, host, address );

		device.register( NodeEvent.ANY, e -> Platform.runLater( this::updateState ) );

		shape.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			if( e.getCode() == KeyCode.EQUALS ) {
				// TODO Add a new child node
			} else if( e.getCode() == KeyCode.MINUS ) {
				// TODO Delete this device and all of its children
			}
		} );

		new FieldInputHandler( shape, name, () -> device.setName( name.getText() ) );
		new FieldInputHandler( shape, host, () -> device.setHost( host.getText() ) );
	}

	NetworkDevice getDevice() {
		return device;
	}

	private void updateState() {
		shape.setFill( getDevice().getResponse().getPaint() );
		name.setText( getDevice().getName() );
		host.setText( getDevice().getHost() );
		address.setText( getDevice().getAddress() );
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
					blur( node );
					action.run();
				}
			} );
			field.editableProperty().addListener( ( p, o, n ) -> {
				if( !n ) {
					blur( node );
					action.run();
				}
			} );
		}

		private void blur( Node node ) {
			node.requestFocus();
		}

	}

}
