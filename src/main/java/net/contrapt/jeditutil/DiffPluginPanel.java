package net.contrapt.jeditutil;

import java.util.List;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

//import org.jmeld.ui.JMeldPanel;
//import org.jmeld.ui.BufferDiffPanel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;

/**
* A plugin panel that shows a JMeld diff of two files.  Any plugin can call this
* with two or three files to show a 2-way or 3-way diff within a jEdit buffer panel.
* Use:
*	  <code>DiffPluginPanel.showDiff(View view, String name, List<String> fileNames)</code>
*/
public class DiffPluginPanel extends PluginPanel {

	//private JMeldPanel jmeldPanel;
	private String name;

	/** The buffer this was called from */
	private Buffer buffer;
	/** The last update time of the buffer (to see if we should reload it)*/
	private long lastModified;
	/** The component to focus on */
	private Component defaultFocus;

	/**
	* Convenience method to add a diff buffer for the given files.	 
	*
	* @param view The jEdit view you are calling from
	* @param name A name for the buffer; it will be appended with ".diff-N" where N
	*				  is a sequence guaranteeing unique buffer names
	* @param fileNames A list of 2 or 3 file names to display the diff for
	*/
	public static void showDiff(View view, String name, List<String> fileNames) {
		DiffPluginPanel panel = new DiffPluginPanel(view, name, fileNames);
		DiffPluginPanel.addPluginPanel(view, panel);
	}

	/**
	* Construct a diff panel and add it as a buffer
	* 
	*/
	private DiffPluginPanel(View view, String name, List<String> fileNames) {
		//this.jmeldPanel = new JMeldPanel();
		this.name = name;
		this.buffer = view.getBuffer();
		this.lastModified = this.buffer.getFile().lastModified();
		//jmeldPanel.openComparison(fileNames);
		setLayout(new BorderLayout());
		// Remove the tool bar
      /*
		for ( Component c : jmeldPanel.getComponents() ) {
			if ( c instanceof JToolBar ) {
				jmeldPanel.remove(c);
			}
			if ( c instanceof JTabbedPane ) {
				defaultFocus = c;
			}
		}
		this.add(jmeldPanel, BorderLayout.CENTER);
		*/
	}

	/**
	* Should focus on the right hand diff panel
	*/
	@Override
	public Component getFocusComponent() {
		return defaultFocus;
	}

	/**
	* Return the name of this plugin panel
	*/
	@Override
	public String getPanelName() {
		return name+".diff";
	}

	/**
	* Cleanup resources when this panel is closed
	*/
	@Override
	public void pluginPanelRemoved() {
		if ( buffer.isClosed() ) return;
		if ( buffer.getFile().lastModified() <= lastModified ) return;
		buffer.reload(jEdit.getActiveView());
	}

	/**
	* Show this panel as a separate buffer
	*/
	@Override
	public boolean showWithBuffer() {
		return false;
	}
}
