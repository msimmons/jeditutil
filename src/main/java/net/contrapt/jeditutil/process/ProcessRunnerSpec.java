package net.contrapt.jeditutil.process;

import java.io.File;
import java.util.Map;

/**
* Interface that describes everything a <code>ProcessRunner</code> needs to
* know in order to run a process.  Clients can implement there own depending
* on needs
*/
public interface ProcessRunnerSpec {

	/**
	* Return the name to be displayed for this OS process
	*/
	public String getName();

	/**
	* Return the arguments needed to build a OS process
	*/
	public String[] getArgs();

	/**
	* Return the directory to run the OS process in
	*/
	public File getDirectory();

	/**
	* Return any environement variable key/value pairs to set on the process
	*/
	public Map<String, String> getEnv();

	/**
	* Return an output handler to process the output from this process
	*/
	public OutputHandler getOutputHandler();

	/**
	* Return whether or not to display this process in the list of running
	* processes
	*/
	public boolean getDisplay();

	/**
	* If this process is a server, return a regular expression that would
	* indicate that the server has started sucessfully.  This is independent
	* of any output handler
	*/
	public String getServerStartedExpression();
}
