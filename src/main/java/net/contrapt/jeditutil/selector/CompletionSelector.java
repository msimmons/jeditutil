package net.contrapt.jeditutil.selector;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.Abbrevs;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.syntax.ParserRuleSet;

/**
* A selector designed for code completion.  Your plugin should extend this as
* it contains several useful methods
*/
public abstract class CompletionSelector<V> extends ValueSelector<Object, V> {
   
   //
   // Properties
   //
   private String defaultValue;
   private View view;
   private Buffer buffer;
   private TextArea textArea;

   //
   // Constructors
   //
   protected CompletionSelector(View view, String defaultValue) {
      this.defaultValue = defaultValue;
      this.view = view;
      this.buffer = view.getBuffer();
      this.textArea = view.getTextArea();
   }

   @Override
   public boolean isCaseSensitive() { return false; }
   
   @Override
   public String getDefault() { return defaultValue; }

   /**
   * Override this method to return a string to be inserted for code completion.
   * By default it returns the selected object's toString().
   */
   public String getCompletionString() {
      return ( getSelectedObject()==null ) ? null : getSelectedObject().toString();
   }

   /**
   * Return keywords for current mode
   */
   protected final List<String> getKeywords() {
      List<String> keywords = new ArrayList<String>();
      Mode mode = buffer.getMode();
      for ( ParserRuleSet set : mode.getTokenMarker().getRuleSets() ) {
         if ( set.getKeywords() == null ) continue;
         for ( String kw : set.getKeywords().getKeywords() ) {
            keywords.add(kw);
         }
      }
      return keywords;
   }

   /**
   * Return any abbreviations for this mode
   */
   protected final Map<String,String> getAbbrevs() {
      Mode mode = buffer.getMode();
      return Abbrevs.getModeAbbrevs().get(mode.getName());
   }

   /**
   * A default code completion selector -- maybe keywords for the mode?
   */
   public static CompletionSelector<String> getDefaultCompletionSelector(final View view, final String defaultValue) {
      return new CompletionSelector<String>(view, defaultValue) {
         public String getTitle() { return "Default Completions"; }
         public Map<String,String> loadValueMap() {
            Map<String,String> keywords = new HashMap<String,String>();
            for ( String kw : getKeywords() ) {
               keywords.put(kw, kw);
            }
            return keywords;
         }
         @Override
         public boolean isCaseSensitive() { return false; }
      };
  }

}
