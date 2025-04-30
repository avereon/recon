package com.avereon.recon;

import com.avereon.zerra.image.RenderedIcon;

public class ReconIcon extends RenderedIcon {

	@Override
	protected void render() {
		double r = 5;

		setStrokeWidth( g( 2 ) );

		// The distance rings
		startPath();
		addArc( g( 16 ), g( 16 ), g( r ), g( r ), 45, 270 );
		draw();

		startPath();
		addArc( g( 16 ), g( 16 ), g( 2 * r ), g( 2 * r ), 45, 270 );
		draw();

		startPath();
		addArc( g( 16 ), g( 16 ), g( 3 * r ), g( 3 * r ), 45, 270 );
		draw();

		// The sweep line
//		startPath(g( 16 ), g( 16 ));
//		addOval( g( 16 ), g( 16 ), g(2),g(2) );
//		fill();

		startPath();
		addLine( g( 16 ), g( 16 ), calcX( g( 3 * r ), 45 ), calcY( g( 3 * r ), 45 ) );
		//closePath();
		draw();

		// The hit dots
		startPath();
		addOval( calcX( g( 2 * r ), 90 ), calcY( g( 2 * r ), 90 ), g( 2 ), g( 2 ) );
		addOval( calcX( g( 2 * r ), 225 ), calcY( g( 2 * r ), 225 ), g( 2 ), g( 2 ) );
		fill();
	}

	private double calcX( double r, double a ) {
		return g( 16 ) + r * Math.cos( Math.toRadians( a ) );
	}

	private double calcY( double r, double a ) {
		return g( 16 ) - r * Math.sin( Math.toRadians( a ) );
	}

	public static void main( String[] args ) {
		proof( new ReconIcon() );
	}

}
