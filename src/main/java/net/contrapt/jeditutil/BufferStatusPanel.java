package net.contrapt.jeditutil;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.List;
import javax.swing.border.Border;
import java.awt.Color;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.plaf.SeparatorUI;
import javax.swing.plaf.basic.BasicSeparatorUI;
import java.net.URL;

import net.contrapt.jeditutil.pluginpanel.PluginPanel;
import net.contrapt.jeditutil.process.ProcessRunner;
import net.contrapt.jeditutil.service.BufferStatusService;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.PluginJAR;
import org.gjt.sp.jedit.GUIUtilities;


/**
 * The <code>BufferStatusPanel</code> appears at the top of the <code>EditPane</code>.
 * It displays the buffer name and status icon; in addition, it displays any status
 * information supplied by other plugins thru the <code>BufferStatusService</code>.
 */
public class BufferStatusPanel extends JPanel {
   
   //
   // PROPERTIES
   //
   /** LED icons for running processes */
//   private static Icon GREEN_ON = getImageIcon("/icons/ball_green.gif");
//   private static Icon GREEN_OFF = getImageIcon("/icons/white-square.jpg");
//   private static Icon RED_ON = getImageIcon("/icons/ball_red.gif");

   /** Allow the user to choose which plugin provided panel to display with this buffer */
   private JButton panelChooser;

   /** The number of processes to display */
   private final static int PROCESS_COUNT = 5;

   /** Display running servers */
   private JLabel[] serverLabels;

   /** Display last N process status */
   private JLabel[] processLabels;

   /** Display count of running processes */
   private JLabel processLabel;

   /** Display number of processes with errors (in the last five finished processs */
   private JLabel errorLabel;

   /** Allow the user to close the current buffer */
   private JButton closeButton;

   /** Displays the buffer name and icon */
   private JLabel bufferLabel;

   /** Stored the plugin provided status components */
   private Map<PluginJAR, JComponent> components;

   /** Collection of panel choices */
   private Collection<PluginPanel> panelChoices;


   //
   // CONSTRUCTORS
   //
   /**
   * Create the status panel.  There is one per <code>EditPane</code>
   */
   public BufferStatusPanel(EditPane pane) {
      initialize(pane);
   }

   //
   // OVERRIDES
   //

   //
   // PUBLIC METHODS
   //

   /**
   * Rest the list of choices for plugin provided panels
   */
   public void resetPanelChoices(Map<Class, PluginPanel> panels) {
      if ( panels == null ) panelChoices = null;
      else panelChoices = panels.values();
      if ( panelChoices == null || panelChoices.size()==0 ) panelChooser.setEnabled(false);
      else panelChooser.setEnabled(true);
   }

   /**
   * Update the buffer status information for the given buffer including
   * requesting each plugin to update its component status
   */
   public void updateBufferStatus(EditPane pane, Buffer buffer, boolean bufferChanging) {
      bufferLabel.setText(buffer.getName());
      bufferLabel.setIcon(buffer.getIcon());
      bufferLabel.setToolTipText(buffer.getPath());
      List<ProcessRunner> servers = ProcessRunner.getRunningServers();
      List<ProcessRunner> processes = ProcessRunner.getLastN(PROCESS_COUNT);
      int i = 0;
      for ( JLabel jl : serverLabels ) {
         if ( i < servers.size() ) {
            jl.setText("S");
            jl.setToolTipText("Server: "+servers.get(i).getName());
            if ( servers.get(i).isStarting() ) jl.setForeground(Color.ORANGE);
            else if ( servers.get(i).isRunning() ) jl.setForeground(Color.GREEN);
            else if ( servers.get(i).hasErrors() ) jl.setForeground(Color.RED);
            else jl.setForeground(Color.BLACK);
         }
         else jl.setText("");
         i++;
      }
      i = 0;
      for ( JLabel jl : processLabels ) {
         if ( i < processes.size() ) {
            jl.setText("P");
            jl.setToolTipText("Process: "+processes.get(i).getName());
            if ( processes.get(i).isStarting() ) jl.setForeground(Color.ORANGE);
            else if ( processes.get(i).isRunning() ) jl.setForeground(Color.GREEN);
            else if ( processes.get(i).hasErrors() ) jl.setForeground(Color.RED);
            else jl.setForeground(Color.BLACK);
         }
         else jl.setText("");
         i++;
      }
      String serviceName = BufferStatusService.class.getName();
      for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
         BufferStatusService service = (BufferStatusService)ServiceManager.getService(serviceName, providerName);
         JComponent component = components.get(service.getPluginJAR());
         if ( component == null ) continue;
         service.update(component, buffer, bufferChanging);
      }
   }

   //
   // PRIVATE METHODS
   //

   /**
   * Initialize variables and layout
   */
   private void initialize(EditPane pane) {
      panelChooser = new JButton(GUIUtilities.loadIcon("ToolbarMenu.gif"));
      panelChooser.setPreferredSize(new Dimension(16,16));
      panelChooser.setEnabled(false);
      panelChooser.setToolTipText("Choose Plugin Panel");
      bufferLabel = new JLabel();
      serverLabels = new JLabel[PROCESS_COUNT];
      processLabels = new JLabel[PROCESS_COUNT];
      for ( int i = 0; i < PROCESS_COUNT; i++ ) {
         serverLabels[i] = new JLabel("");
      }
      for ( int i = 0; i < PROCESS_COUNT; i++ ) {
         processLabels[i] = new JLabel("");
      }
//      processLabel = new JLabel("0");
//      processLabel.setForeground(Color.GREEN);
//      processLabel.setToolTipText("Number of running processes");
//      errorLabel = new JLabel("0");
//      errorLabel.setForeground(Color.RED);
//      errorLabel.setToolTipText("Number of last 5 processes with errors");
      // Set the font on these components
/*
      float baseFontSize = 12.0f;
      try { baseFontSize = Float.valueOf(jEdit.getProperty("view.fontsize")); }
      catch (NumberFormatException e) {
         System.out.println("Error getting base font size, using "+baseFontSize);
      }
      float scaledFontSize = baseFontSize*.8f;
      panelChooser.setFont(panelChooser.getFont().deriveFont(scaledFontSize));
      closeButton.setFont(closeButton.getFont().deriveFont(scaledFontSize));
*/
      // Add components
      setLayout(new FlowLayout(FlowLayout.LEFT));
      add(bufferLabel);
      add(panelChooser);
      for ( JLabel jl : serverLabels ) {
         add(jl);
      }
      for ( JLabel jl : processLabels ) {
         add(jl);
      }
//      add(processLabel);
//      add(errorLabel);
      JSeparator sep = new JSeparator(JSeparator.VERTICAL);
      sep.setUI(new BasicSeparatorUI());
      add(sep);
      // Add plugin components
      addPluginComponents(pane);
      // Create and add a border to the panel
      JEditTextArea textArea = pane.getTextArea();
      Border border = textArea.getBorder();
      setBorder(border);
      getInsets().bottom=0;
      getInsets().top=0;
      setBackground(Color.WHITE);
   }

   /**
   * Add components supplied by plugins to the status panel
   */
   public void addPluginComponents(EditPane pane) {
      if ( components == null ) components = new HashMap<PluginJAR, JComponent>();
      String serviceName = BufferStatusService.class.getName();
      for ( String providerName : ServiceManager.getServiceNames(serviceName) ) {
         BufferStatusService service = (BufferStatusService)ServiceManager.getService(serviceName, providerName);
         if ( components.containsKey(service.getPluginJAR()) ) continue;
         JComponent component = service.getComponent();
         if ( component == null ) continue;
         add(component);
         add(new JSeparator(JSeparator.VERTICAL));
         components.put(service.getPluginJAR(), component);
         revalidate();
      }
   }

   /**
   * Remove the component for the given plugin
   */
   public void removePluginComponent(PluginJAR jar) {
      JComponent component = components.remove(jar);
      if ( component == null ) return;
      remove(component);
   }

   /**
   * Create an icon for the given image
   */
   private static ImageIcon getImageIcon(String path) {
      URL url = BufferStatusPanel.class.getResource(path);
      if ( url != null ) {
         System.out.println("Found image "+path+" at URL "+url);
         return new ImageIcon(url, path);
      }
      else {
         System.out.println("Couldn't find icon "+path+" at URL "+url);
         return null;
      }
   }

}
