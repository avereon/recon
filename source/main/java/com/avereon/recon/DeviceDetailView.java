package com.avereon.recon;

import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import lombok.CustomLog;

@CustomLog
public class DeviceDetailView extends VBox {

	private static final double DEFAULT_WIDTH = 150;

	private final TextField group;

	private final TextField name;

	private final TextField host;

	private final Label address;

	private final Label message;

	public DeviceDetailView( NetworkDevice device ) {
		group = new TextField( device.getGroup() );
		name = new TextField( device.getName() );
		host = new TextField( device.getHost() );
		address = new Label( device.getAddress() );
		message = new Label( device.getMessage() );

		setPrefWidth( DEFAULT_WIDTH );
		getStyleClass().add( "device-details" );
		setAlignment( Pos.CENTER_LEFT );
		getChildren().addAll( group, name, host, address, message );
		setVisible( false );
		setManaged( false );
		setViewOrder( -1 );
		setFocusTraversable( true );

		device.register( NetworkDevice.GROUP, e -> Fx.run( () -> group.setText( e.getNewValue() ) ) );
		device.register( NetworkDevice.NAME, e -> Fx.run( () -> name.setText( e.getNewValue() ) ) );
		device.register( NetworkDevice.HOST, e -> Fx.run( () -> host.setText( e.getNewValue() ) ) );
		device.register( NetworkDevice.IPV4, e -> Fx.run( () -> address.setText( e.getNewValue() ) ) );
		device.register( NetworkDevice.MESSAGE, e -> Fx.run( () -> message.setText( e.getNewValue() ) ) );

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
