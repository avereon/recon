package com.avereon.recon;

import javafx.application.Platform;
import javafx.geometry.Pos;
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

		shape = new Circle( 30, DeviceResponse.UNKNOWN.getPaint() );

		name = new TextField( device.getName() );
		name.setAlignment( Pos.CENTER );
		name.setEditable( false );

		host = new TextField( device.getHost() );
		host.setAlignment( Pos.CENTER );
		host.setEditable( false );

		address = new Label( device.getAddress() );
		address.setAlignment( Pos.CENTER );

		setAlignment( Pos.CENTER );
		getChildren().addAll( shape, name, host, address );

		device.addNodeListener( e -> Platform.runLater( this::updateState ) );

		new FieldInputHandler( name, () -> device.setName( name.getText() ) );
		new FieldInputHandler( host, () -> device.setHost( host.getText() ) );
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

		public FieldInputHandler( TextField field, Runnable action ) {
			field.addEventHandler( MouseEvent.MOUSE_CLICKED, e -> {
				priorValue = field.getText();
				field.setEditable( true );
			} );
			field.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
				if( e.getCode() == KeyCode.ESCAPE ) {
					field.setText( priorValue );
					field.setEditable( false );
				}
				if( e.getCode() == KeyCode.ENTER ) {
					field.setEditable( false );
					action.run();
				}
			} );
			field.editableProperty().addListener( ( p, o, n ) -> {
				if( !n ) {
					field.setEditable( false );
					action.run();
				}
			} );
		}
	}

}
