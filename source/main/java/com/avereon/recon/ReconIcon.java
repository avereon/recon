package com.avereon.recon;

import com.avereon.venza.image.ProgramIcon;
import javafx.scene.shape.ArcType;

public class ReconIcon extends ProgramIcon {

	@Override
	protected void render() {
		double r = 5;

		// The distance rings
		drawCenteredArc( g( 16 ), g( 16 ), g( r ), g( r ), 45, 270, ArcType.OPEN );
		drawCenteredArc( g( 16 ), g( 16 ), g( 2 * r ), g( 2 * r ), 45, 270, ArcType.OPEN );
		drawCenteredArc( g( 16 ), g( 16 ), g( 3 * r ), g( 3 * r ), 45, 270, ArcType.OPEN );

		// The sweep line
		drawDot( g( 16 ), g( 16 ) );
		drawLine( g( 16 ), g( 16 ), calcX( g( 3 * r ), 45 ), calcY( g( 3 * r ), 45 ) );

		// The hit dots
		drawAndFillDot( calcX( g( 2 * r ), 90 ), calcY( g( 2 * r ), 90 ), g( 2 ) );
		drawAndFillDot( calcX( g( 2 * r ), 225 ), calcY( g( 2 * r ), 225 ), g( 2 ) );
	}

	private void drawAndFillDot( double x, double y, double r ) {
		fillCenteredOval( x, y, r, r );
		drawCenteredOval( x, y, r, r );
	}

	private double calcX( double r, double a ) {
		return g( 16 ) + r * Math.cos( a * RADIANS_PER_DEGREE );
	}

	private double calcY( double r, double a ) {
		return g( 16 ) - r * Math.sin( a * RADIANS_PER_DEGREE );
	}

	public static void main( String[] args ) {
		proof( new ReconIcon() );
	}

}
