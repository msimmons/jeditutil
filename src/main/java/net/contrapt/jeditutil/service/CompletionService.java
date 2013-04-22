package net.contrapt.jeditutil.service;

import net.contrapt.jeditutil.selector.CompletionSelector;
import org.gjt.sp.jedit.View;

/**
* This interface describes a service that supports code completion.  Each 
* implementor defines the mode(s) that it provides code completion for and 
* when requested returns a <code>ValueSelector</code> for a given buffer.  The
* values are displayed in a <code>CompletionDialog</code>
*/
public interface CompletionService {

   /**
   * Return whether or not the given mode is supported by this provider
   *
   * @param mode The name of the mode in question
   * @return whether this service provider supports the mode
   */
   public boolean supportsMode(String mode);

   /**
   * Tell the plugin that the buffer requires parsing --
   * usually in response to saving the buffer
   *
   * @param view The current view
   */
//   public void parseBuffer(Buffer buffer);

   /**
   * Return strings of characters that should trigger a code completion popup for each
   * mode supported
   *
   * @return A map of mode name to a string of characters that trigger code popups
   */
//   public Map<String,String> getCompletionTriggers();

   /**
   * Return a the values that the user can choose from for the given buffer.
   * The value of <code>ValueSelector.getSelectedObject().toString()</code>
   * will be inserted in the buffer at the current position upon user choice.
   *
   * @param view The current view
   * @return A <code>CompletionSelector</code> of possible completions
   */
   public CompletionSelector getCompletionSelector(View view);

}
