package net.contrapt.jeditutil.selector;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.awt.Color;

import net.contrapt.jeditutil.process.ProcessRunner;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.jEdit;

/**
 * Base class for various jEdit specific value selectors
 */
public abstract class UtilSelector<P, V> extends ValueSelector<P, V> {

   //
   // Properties
   //
   protected String defaultValue;

   //
   // Constructors
   //
   protected UtilSelector(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public boolean isCaseSensitive() {
      return false;
   }

}
