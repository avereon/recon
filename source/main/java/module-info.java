import com.avereon.xenon.Module;

module com.avereon.recon {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires com.avereon.xenon;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	opens com.avereon.recon.bundles;
	exports com.avereon.recon to com.avereon.xenon, com.avereon.zerra;
	provides Module with com.avereon.recon.Recon;

}
