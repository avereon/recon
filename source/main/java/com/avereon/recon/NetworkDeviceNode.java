package com.avereon.recon;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

class NetworkDeviceNode extends VBox {

	private NetworkDevice device;

	private Shape shape;

	private Label name;

	private Label host;

	private Label address;

	public NetworkDeviceNode( NetworkDevice device ) {
		this.device = device;

		this.shape = new Circle( 30, DeviceResponse.UNKNOWN.getPaint() );
		this.name = new Label( device.getName() );
		this.host = new Label( device.getHost() );
		this.address = new Label( device.getAddress() );

		setAlignment( Pos.CENTER );
		getChildren().addAll( shape, name, host, address );

		device.addNodeListener( e -> Platform.runLater( this::updateState ) );

		updateState();
	}

	private void updateState() {
		shape.setFill( getDevice().getResponse().getPaint() );
		name.setText( getDevice().getName() );
		host.setText( getDevice().getHost() );
		address.setText( getDevice().getAddress() );
	}

	NetworkDevice getDevice() {
		return device;
	}

}
