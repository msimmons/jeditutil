package net.contrapt.jeditutil;

import java.util.Set;
import java.util.LinkedHashSet;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.Buffer;

/**
* Manage navigation history so that the user can use key strokes to navigate
* backwards and forwards thru history.  Navigation events are defined as plugins
* call the methods of this class to add history entries.  Therefore, the 
* behaviour of navigation history will depend on what plugin features a user has
* used to navigate to different buffers.  This class simply keeps track of the
* history.
*/
public class NavigationHistoryManager {

   /** Default maximum history limit */
   public static final int DEFAULT_HISTORY_LIMIT = 40;

   /** A set of entries is kept for each context to prevent dups */
   private Set<HistoryEntry> entries;

   /** Current history entry is kept for each context */
   private HistoryEntry currentEntry;

   /** Maximum number of history entries to keep */
   private int historyLimit = DEFAULT_HISTORY_LIMIT;

   /**
   * 
   */
   public NavigationHistoryManager() {
      entries = new LinkedHashSet<HistoryEntry>();
   }

   /**
   * Add a history entry.  A history entry is a buffer and a caret position within
   * that buffer
   *
   * @param view The calling view
   */
   public void addEntry(View view) {
      Buffer buffer = view.getBuffer();
      int caret = view.getTextArea().getCaretPosition();
      HistoryEntry entry = new HistoryEntry(buffer, caret);
      if ( entryExists(entry) ) return;
      entries.add(entry);
      entry.insertAfter(currentEntry);
      currentEntry = entry;
      checkHistoryLimit();
   }

   /**
   * Remove the given history entry
   */
   public void removeEntry(HistoryEntry entry) {
      if ( !entries.remove(entry) ) return;
      if ( entry.equals(currentEntry) ) currentEntry = entry.next;
      entry.remove();
   }

   /**
   * Go forwards in history to the next entry if it exists
   *
   * @param context Optional context; if null, defaults to global context
   */
   public void navigateForwards(View view) {
      if ( currentEntry == null ) return;
      if ( currentEntry.next == null ) navigateTo(view, currentEntry);
      else navigateTo(view, currentEntry.next);
   }

   /**
   * Go backwards in history to the previous entry if it exists
   *
   * @param context Optional context; if null, defaults to global context
   */
   public void navigateBackwards(View view) {
      if ( currentEntry == null ) return;
      if ( currentEntry.prev == null ) navigateTo(view, currentEntry);
      else navigateTo(view, currentEntry.prev);
   }

   /**
   * Check if the given entry exists alreay; if so, set it to the current entry
   */
   private boolean entryExists(HistoryEntry entry) {
      if ( !entries.contains(entry) ) return false;
      for ( HistoryEntry e : entries ) {
         if ( e.equals(entry) ) {
            currentEntry = e;
            return true;
         }
      }
      return false;
   }

   /**
   * Navigate to the given history entry
   */
   private void navigateTo(View view, HistoryEntry entry) {
      if ( entry.buffer.isClosed() ) {
         entry.buffer = jEdit.openFile(view, entry.buffer.getPath());
      }
      view.goToBuffer(entry.buffer);
      try {
         view.getTextArea().moveCaretPosition(entry.caret);
      }
      catch (NullPointerException e) {
         // Possible bug in jEdit?  Get this when a buffer is closed
      }
      currentEntry = entry;
   }

   /**
   * Check the history limit and remove an entry if it is exceeded
   */
   private void checkHistoryLimit() {
      if ( entries.size() < historyLimit ) return;
      // Remove the earliest entry
      for ( HistoryEntry entry : entries ) {
         removeEntry(entry);
         break;
      }
   }

   /**
   * Holds history info
   */
   private static class HistoryEntry {
      Buffer buffer;
      int caret;
      HistoryEntry prev;
      HistoryEntry next;

      HistoryEntry(Buffer buffer, int caret) {
         this.buffer = buffer;
         this.caret = caret;
      }

      /**
      * Link up the nodes
      */
      void insertAfter(HistoryEntry before) {
         this.prev = before;
         if ( before == null ) return;
         this.next = before.next;
         before.next = this;
         if ( this.next == null ) return;
         this.next.prev = this;
      }

      /**
      * Remove this node
      */
      void remove() {
         if ( prev != null ) {
            prev.next = next;
         }
         if ( next != null ) {
            next.prev = prev;
         }
         prev = null;
         next = null;
      }

      @Override
      public int hashCode() {
         return (buffer.getPath()+"::"+caret).hashCode();
      }

      @Override
      public boolean equals(Object o) {
         if ( !(o instanceof HistoryEntry) ) return false;
         HistoryEntry other = (HistoryEntry)o;
         return buffer.getPath().equals(other.buffer.getPath()) && caret==other.caret;
      }

      @Override
      public String toString() {
         return "["+buffer+"::"+caret+"]";
      }
   }

}
