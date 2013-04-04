package net.contrapt.jeditutil;

/**
* A default implementation of <code>OutputHandler</code> which does nothing; used in case
* a null handler is passed into the <code>ProcessRunner</code>
*/
public class DefaultOutputHandler implements OutputHandler {

   public void processLine(String line) {
   }

   public void finish() {
   }

   public String getErrorMessage() {
      return null;
   }

   public int getErrorCount() {
      return 0;
   }

}
