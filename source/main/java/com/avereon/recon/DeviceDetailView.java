package com.avereon.recon;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class DeviceDetailView extends VBox {

	private final TextField group;

	private final TextField name;

	private final TextField host;

	private final Label address;

	public DeviceDetailView( NetworkDevice device ) {
		group = new TextField( device.getGroup() );
		name = new TextField( device.getName() );
		host = new TextField( device.getHost() );
		address = new Label( device.getAddress() );

		getStyleClass().add( "network-device-details" );
		setStyle( "-fx-background-color: #80000040;" );
		setAlignment( Pos.CENTER_LEFT );
		getChildren().addAll( group, name, host, address );
		//setVisible( false );
		setManaged( false );
		//setViewOrder( -1 );
		setFocusTraversable( true );

		device.register( "group", e -> group.setText( e.getNewValue() ) );
		device.register( "name", e -> name.setText( e.getNewValue() ) );
		device.register( "host", e -> host.setText( e.getNewValue() ) );
		device.register( "address", e -> address.setText( e.getNewValue() ) );

		new FieldInputHandler( group, () -> device.setGroup( group.getText() ) );
		new FieldInputHandler( name, () -> device.setName( name.getText() ) );
		new FieldInputHandler( host, () -> device.setHost( host.getText() ) );

		addEventHandler( KeyEvent.KEY_PRESSED, e -> {
			if( e.getCode() == KeyCode.ESCAPE ) setVisible( false );
		} );
	}

	private class FieldInputHandler {

		private String priorValue;

		private FieldInputHandler( TextField field, Runnable action ) {
			field.addEventHandler( KeyEvent.KEY_PRESSED, e -> {
				if( e.getCode() == KeyCode.ESCAPE ) {
					field.setText( priorValue );
					requestFocus();
					e.consume();
				}
				if( e.getCode() == KeyCode.ENTER ) {
					action.run();
					requestFocus();
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
