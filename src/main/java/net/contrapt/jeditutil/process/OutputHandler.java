package net.contrapt.jeditutil.process;

/**
* A class that processes output from a running process; this could be filtering for 
* error information or status information.  Implementations should be provided by 
* the client that calls <code>ProcessRunner.runProcess</code>
*/
public interface OutputHandler {

   /**
   * Process a line of output
   *
   * @param line The line of output to process
   */
   public void processLine(String line);

   /**
   * Called by <code>ProcessRunner</code> when all output to process
   * is finished
   */
   public void finish();

   /**
   * Return an error message for the handler
   */
   public String getErrorMessage();

   /**
   * Return a count of errors for this handler
   */
   public int getErrorCount();

}
