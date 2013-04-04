package net.contrapt.jeditutil;

import java.io.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.regex.Pattern;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

/**
* This class runs a command externally in OS process.	 It implements <code>Runnable</code>,
* so the client can run it in a separate thread or directly by calling run(). It also supports
* displaying the process in a list of running processes, viewing the output of the process
* in a buffer, applying the given <code>OutputHandler</code> to the output to perform
* a client specific task
*/
public class ProcessRunner implements Runnable {
	
	//
	// Properties
	//
	private static ProcessRunnerListModel listModel = new ProcessRunnerListModel();
	private static ProcessRunnerMouseListener mouseListener = new ProcessRunnerMouseListener();
	private static ProcessRunnerKeyListener keyListener = new ProcessRunnerKeyListener(); 
	private static int DEFAULT_LIST_SIZE = 20;
	private static int DEFAULT_TAIL_LINES = 5000;
	private static int listSize = DEFAULT_LIST_SIZE;
	private static int bufferCount = 0;

	private String name;
	private ProcessBuilder builder;
	private Process process;
	private Thread thread;
	private OutputHandler handler;
	private boolean display;
	private boolean isServer=false;
	private Pattern serverStartedPattern;
	private int status = 0;
	private Exception exception;
	private StateEnum state=StateEnum.RUNNING;
	private String description;
	private File outputFile;
	private BufferedWriter output;
	private int linesWritten;
	private Buffer buffer;
	private ProcessRunnerInfoPanel infoPanel;
	private int tailLines = DEFAULT_TAIL_LINES;
	private boolean bufferChanged = false;
	private boolean killed = false;
	private int sequence;

	private enum StateEnum {
		STARTING,
		RUNNING,
		WAITING,
		DONE;
	}

	/**
	* Run the given process; send the output through the given output handler
	*
	* @param name The name of the process
	* @param builder A process builder for the OS process
	* @param handler An output handler to do whatever it wants with the command output
	* @param display Whether to add this process to the visible list of running processes or not
	*/
	public static ProcessRunner create(ProcessRunnerSpec spec) {
		ProcessRunner runner = new ProcessRunner(spec);
		return runner;
	}

	/** Return the list model for the list of running processes */
	static ListModel getListModel() { return listModel; }

	/** Return a mouse listeners for mouse events on the list */
	static MouseListener getMouseListener() { return mouseListener; }

	/** Return a key listener for the list of processes */
	static KeyListener getKeyListener() { return keyListener; }

	/** 
	* Set the maximum size of the process list; old processes are removed after list reaches this size
	*/
	public static void setListSize(int maxListSize) {
		listSize = maxListSize;
	}

	/**
	* Return the list of running processes
	*/
	public static List<ProcessRunner> getProcesses() {
		return listModel.getProcesses();
	}

	/**
	* Return a list of the latest N processes
	*/
	public static List<ProcessRunner> getLastN(int N) {
		return listModel.getLastN(N);
	}
	
	/**
	* Return a list of any servers that are running
	*/
	public static List<ProcessRunner> getRunningServers() {
		return listModel.getRunningServers();
	}

	/**
	* Clean up resources when shutdown
	*/
	static void shutdown() {
		listModel.clear();
	}

	//
	// Constructors
	//
	protected ProcessRunner(ProcessRunnerSpec spec) {
		this.name = spec.getName();
		this.builder = createProcessBuilder(spec);
		this.handler = ( spec.getOutputHandler()==null ) ? new DefaultOutputHandler() : spec.getOutputHandler();
		this.display = spec.getDisplay();
		setServerStartedExpression(spec.getServerStartedExpression());
		this.sequence = ++bufferCount;
	}

	/**
	* Create the <code>ProcessBuilder</code> from the given specification
	*/
	private ProcessBuilder createProcessBuilder(ProcessRunnerSpec spec) {
      ProcessBuilder process = new ProcessBuilder(spec.getArgs());
      process.redirectErrorStream(true);
      process.directory(spec.getDirectory());
      for ( String name : spec.getEnv().keySet() ) {
         String value = spec.getEnv().get(name);
         process.environment().put(name, value);
      }
		return process;
	}

	/**
	* Indicate that this process is a server process and supply a regular expression matching
	* a line in the output that determines when the server has started succesfully
	*/
	private void setServerStartedExpression(String serverStartedExpression) {
		if ( serverStartedExpression == null ) return;
		isServer = true;
		serverStartedPattern = Pattern.compile(serverStartedExpression);
		state = StateEnum.STARTING;
	}

	/**
	* Is this a server?
	*/
	public boolean isServer() {
		return isServer;
	}

	/**
	* Get the name of this process
	*/
	public String getName() {
		return name;
	}

	/**
	* Return the exception encountered while trying to run the process if any
	*/
	public Exception getException() { 
		return exception; 
	}

	/**
	* Return the description string which will have the command, the output file
	* name and possibly exception or stack trace info
	*/
	public String getDescription() {
		return description;
	}

	/**
	* Return the status code retured by the OS process
	*/
	public int status() { 
		return status; 
	}

	/**
	* Did this process have any errors?
	*/
	public boolean hasErrors() {
		return ( exception != null || status != 0 || handler.getErrorCount() > 0 );
	}

	/**
	* Is it a server that is starting
	*/
	public boolean isStarting() {
		return ( state == StateEnum.STARTING );
	}

	/**
	* Is this process still running?
	*/
	public boolean isRunning() { 
		return (state == StateEnum.RUNNING || state == StateEnum.STARTING); 
	}

	/**
	* Set the number of lines to tail output; 0 for no tail
	*
	* @param tailLines The number of output lines to tail or 0 for no tail
	*/
	public void setTailLines(int tailLines) {
		this.tailLines = tailLines;
	}

	/**
	* Implement <code>Runnable</code>
	*/
	public void run() {
		thread = Thread.currentThread();
		if ( display ) listModel.add(this);
		description = builder.command().toString();
		try {
			outputFile = File.createTempFile(name.replace(':','.'), null);
			outputFile.deleteOnExit();
			output = new BufferedWriter(new FileWriter(outputFile));
			description += "\n"+outputFile;
		}
		catch (Exception e) {
			exception = e;
			String msg = "Error creating output file: "+e;
			description += "\n"+msg;
			for ( StackTraceElement el : e.getStackTrace() ) description += "\n	 "+el;
			processFinished(StateEnum.DONE);
			bufferChanged = true;
			return;
		}
		try {
			process = builder.start();
		}
		catch (Exception e) {
			exception = e;
			String msg = "Error starting process: "+e;
			description += "\n+"+msg;
			for ( StackTraceElement el : e.getStackTrace() ) description +="\n	"+el;
			processFinished(StateEnum.DONE);
			bufferChanged = true;
			try { output.close(); }
			catch (Exception e1) {}
			output=null;
			return;
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			processStarted();
			while ( true ) {
				line = in.readLine();
				if ( line == null ) break;
				synchronized(outputFile) {
					output.write(line);
					output.newLine();
				}
				linesWritten++;
				bufferChanged = true;
				handler.processLine(line);
				checkServerStarted(line);
				if ( killed ) break;
			}
			processFinished(StateEnum.WAITING);
			status = process.waitFor();
		}
		catch (Exception e) {
			exception = e;
			String msg = "Error processing command output : "+e;
			description += "\n"+msg;
			for ( StackTraceElement el : e.getStackTrace() ) description += "\n	 "+el;
			bufferChanged = true;
		}
		finally {
			handler.finish();
			try { in.close(); }
			catch (Exception e) {}
			try { output.close(); }
			catch (Exception e) {}
			output = null;
			in = null;
			Log.log(Log.DEBUG, this, "Finished running process:\n "+description);
			processFinished(StateEnum.DONE);
		}
	}

	/**
	* Indicate that a process has started
	*/
	private void processStarted() {
		View view = jEdit.getActiveView();
		view.getStatus().setMessageAndClear(toString());
		UtilPlugin.processStarted();
	}

	/**
	* If the process is a server, check if it has started
	*/
	private void checkServerStarted(String line) {
		if ( !isServer ) return;
		if ( state != StateEnum.STARTING ) return;
		if ( serverStartedPattern == null ) return;
		if ( serverStartedPattern.matcher(line).matches() ) {
			state = StateEnum.RUNNING;
			if ( display ) listModel.update(this);
			jEdit.getActiveView().getStatus().setMessageAndClear(toString());
			UtilPlugin.processStarted();
		}
	}

	/**
	* Update list model and set status when process finishes
	*/
	private void processFinished(StateEnum state) {
		this.state = state;
		View view = jEdit.getActiveView();
		if ( display ) listModel.update(this);
		if ( state == StateEnum.DONE ) {
			view.getStatus().setMessage(toString());
			UtilPlugin.processFinished();
		}
	}

	/**
	* Display or close buffer for this runners output
	*/
	private void toggleBuffer() {
		View view = jEdit.getActiveView();
		if ( buffer == null || buffer.isClosed() ) {
			buffer = jEdit.openTemporary(view, null, name+"-"+sequence, true);
			tailOutput();
			jEdit.commitTemporary(buffer);
			view.getEditPane().setBuffer(buffer, true);
			view.getTextArea().goToBufferStart(false);
			infoPanel = new ProcessRunnerInfoPanel(this);
			infoPanel.addPluginPanel(view, infoPanel);
		}
		else {
			jEdit._closeBuffer(view, buffer);
			buffer = null;
			infoPanel = null;
		}
		listModel.update(this);
	}

	/**
	* Go to the runners buffer if it is open
	*/
	private void goToBuffer() {
		if ( buffer == null || buffer.isClosed() ) return;
		View view = jEdit.getActiveView();
		view.goToBuffer(buffer);
	}

	/**
	* Close the processes buffer if it is open
	*/
	void closeBuffer() {
		if ( buffer == null || buffer.isClosed() ) return;
		toggleBuffer();
	}

	/**
	* Open or refresh the buffer for this process
	*/
	void loadBuffer() {
		if ( buffer == null || buffer.isClosed() ) toggleBuffer();
		else reloadBuffer();
	}

	/**
	* Refresh the buffer if it is open and has been tailed
	*/
	private void reloadBuffer() {
		if ( buffer == null || buffer.isClosed() ) return;
		if ( bufferChanged ) tailOutput();
		View view = jEdit.getActiveView();
		view.getEditPane().setBuffer(buffer, true);
		view.getTextArea().goToBufferEnd(false);
	}

	/**
	* Insert the appropriate lines from the output to implement tailing
	*/
	private void tailOutput() {
		if ( outputFile == null ) return;
		if ( buffer == null || buffer.isClosed() ) return;
		bufferChanged = false;
		buffer.setReadOnly(false);
		int position = 0;
		int linesOverLimit = linesWritten-tailLines;
		int removeToOffset = (linesOverLimit > 0 && buffer.getLength() > 0) ? buffer.getLineEndOffset(linesOverLimit) : 0;
		buffer.remove(position, removeToOffset);
		position = buffer.getLength();
		int skipLines = (linesOverLimit < 0) ? buffer.getLineCount() : linesOverLimit+buffer.getLineCount();
		BufferedReader tail = null;
		try {
			if ( output != null ) output.flush();
			tail = new BufferedReader(new FileReader(outputFile));
			synchronized(outputFile) {
				while ( true ) {
					String line = tail.readLine();
					if ( line == null ) break;
					if ( --skipLines > 0 ) continue;
					buffer.insert(position, line+"\n");
					position += line.length()+1;
				}
			}
		}
		catch (IOException e) {
			buffer.insert(position, "Error tailing file "+outputFile+": "+e);
		}
		finally {
			try { tail.close(); }
			catch (Exception e) {}
		}
		buffer.setDirty(false);
		buffer.setReadOnly(true);
	}

	/**
	* Kill the process associated with this runner
	*/
	void killProcess() {
		if ( killed ) return;
		if ( thread != null ) thread.interrupt();
		if ( process != null ) process.destroy();
		killed = true;
	}

	public String getStatusString() {
		StringBuilder string = new StringBuilder("");
		if ( status != 0 ) string.append(";status="+status);
		if ( handler.getErrorCount() != 0 ) string.append(";errors="+handler.getErrorCount());
		if ( handler.getErrorMessage() != null ) string.append(";"+handler.getErrorMessage());
		if ( exception != null ) string.append(";"+exception.getClass());
		if ( buffer != null && !buffer.isClosed() ) string.append(";"+"OPEN");
		return string.toString();
	}

	@Override
	public String toString() {
		return name+"-"+sequence+" ["+state+getStatusString()+"]";
	}

	private static void handleSelected(JList list) {
		int index = list.getSelectedIndex();
		if ( index < 0 ) return;
		ProcessRunner runner = listModel.get(index);
		if ( runner != null ) runner.toggleBuffer();
		list.requestFocusInWindow();
	}

	private static void handleChosen(JList list) {
		int index = list.getSelectedIndex();
		if ( index < 0 ) return;
		ProcessRunner runner = listModel.get(index);
		if ( runner != null ) {
			runner.reloadBuffer();
			runner.goToBuffer();
		}
		list.requestFocusInWindow();
	}

	private static void killProcess(JList list) {
		int index = list.getSelectedIndex();
		if ( index < 0 ) return;
		ProcessRunner runner = listModel.get(index);
		if ( runner != null ) runner.killProcess();
	}

	private static void reloadBuffer(JList list) {
		int index = list.getSelectedIndex();
		if ( index < 0 ) return;
		ProcessRunner runner = listModel.get(index);
		if ( runner != null ) runner.reloadBuffer();
	}

	/**
	* Define a list model for tool runners; this model will drive list display for 
	* project status panel
	*/
	static class ProcessRunnerListModel extends AbstractListModel {
		private List<ProcessRunner> runners;
		
		ProcessRunnerListModel() {
			runners = new ArrayList<ProcessRunner>();
		}

		public ProcessRunner get(int index) {
			if ( index >= runners.size() ) return null;
			return runners.get(index);
		}

		public Object getElementAt(int index) {
			return get(index);
		}

		public int getSize() {
			return runners.size();
		}

		/**
		* Return the list of processes
		*/
		List<ProcessRunner> getProcesses() {
			return runners;
		}

		/**
		* Return the last N processes
		*/
		List<ProcessRunner> getLastN(int N) {
			List<ProcessRunner> lastN = new ArrayList<ProcessRunner>(N);
			for ( ProcessRunner pr : runners ) {
				if ( lastN.size() > N ) break;
				if ( pr.isServer() ) continue;
				lastN.add(pr);
			}
			return lastN;
		}

		/**
		* Return any running servers
		*/
		List<ProcessRunner> getRunningServers() {
			List<ProcessRunner> servers = new ArrayList<ProcessRunner>();
			for ( ProcessRunner pr : runners ) {
				if ( !pr.isServer() ) continue;
				if ( !pr.isRunning() ) continue;
				servers.add(pr);
			}
			return servers;
		}

		/**
		* Add a tool runner to the list model
		*/
		void add(ProcessRunner runner) {
			runners.add(0, runner);
			trimList();
			fireIntervalAdded(this, 0, runners.size()-1);
		}

		/**
		* Trim the list of processes to a given size
		*/
		private void trimList() {
			if ( runners.size() < listSize ) return;
			for ( int i=runners.size()-1; i > -1; i-- ) {
				if ( runners.get(i).isRunning() ) continue;
				runners.remove(i);
				break;
			}
		}

		/**
		* Called when a tool's status is updated
		*/
		void update(ProcessRunner runner) {
			fireContentsChanged(this, 0, runners.indexOf(runner));
		}
		
		/**
		* Clear all entries in this model
		*/
		void clear() {
			for ( ProcessRunner runner : runners ) {
				if ( runner.isRunning() ) runner.killProcess();
			}
			int size = runners.size();
			runners.clear();
			if ( size > 0 ) fireIntervalRemoved(this, 0, size-1);
		}

		/**
		* Return whether the last finished process had an error
		*/
		boolean currentHasError() {
			if ( runners.size()==0 ) return false;
			if ( runners.get(0).isRunning() ) return false;
			return runners.get(0).hasErrors();
		}
	}

	/**
	* A mouse listener for the value list
	*/
	static class ProcessRunnerMouseListener extends MouseAdapter {
		/**
		* Select a list item by double clicking
		*/
		@Override
		public void mouseClicked(MouseEvent argEvent) {
			Object o = argEvent.getSource();
			if ( !(o instanceof JList) ) return;
			if (argEvent.getClickCount() == 2) {
				handleSelected((JList)o);
			}
			else if ( argEvent.getClickCount() == 1) {
				handleChosen((JList)o);
			}
		}
	}

	/**
	* A keyboard listener for the value list
	*/
	static class ProcessRunnerKeyListener extends KeyAdapter {

		/**
		* Do various actions when key is pressed
		*/
		@Override
		public void keyPressed(KeyEvent argEvent) {
			int iKeyCode = argEvent.getKeyCode();
			int iMask = argEvent.getModifiers();
			Object o = argEvent.getSource();
			if ( !(o instanceof JList) ) return;
			switch ( iKeyCode ) {
				case KeyEvent.VK_K:
					killProcess((JList)o);
					argEvent.consume();
					break;
				case KeyEvent.VK_R:
					reloadBuffer((JList)o);
					argEvent.consume();
					break;
				case KeyEvent.VK_ENTER:
					handleSelected((JList)o);
					argEvent.consume();
					break;
				case KeyEvent.VK_SPACE:
					handleChosen((JList)o);
					argEvent.consume();
					break;
			}
		}
	}

	/**
	* A plugin panel that shows 1) the command that was run; 2) the output file name
	* 3) a button to refresh the buffer
	*/
	static class ProcessRunnerInfoPanel extends PluginPanel {
		
		private ProcessRunner runner;

		ProcessRunnerInfoPanel(ProcessRunner runner) {
			this.runner = runner;
			initialize();
		}

		/**
		* Create and layout components
		*/
		private void initialize() {
			String description = runner.getDescription();
			String[] lines = description.split("\n");
			int columns = 0;
			for ( String line : lines ) columns = (columns>line.length()) ? columns : line.length();
			JTextArea ta = new JTextArea(description, lines.length, columns);
			ta.setEditable(false);
			JToolBar tb = new JToolBar();
			tb.add(ta);
			tb.addSeparator();
			// Action to refresh output
			Action a = new AbstractAction("Refresh") {
				public void actionPerformed(ActionEvent e) {
					runner.loadBuffer();
				}
			};
			(tb.add(a)).setMnemonic(KeyEvent.VK_R);
			// An action to kill the process
			a = new AbstractAction("Kill") {
				public void actionPerformed(ActionEvent e) {
					runner.killProcess();
				}
			};
			(tb.add(a)).setMnemonic(KeyEvent.VK_K);
			//An action for opening the actual log file (rather than tailing)
			a = new AbstractAction("Open File") {
				public void actionPerformed(ActionEvent e) {
					runner.killProcess();
				}
			};
			(tb.add(a)).setMnemonic(KeyEvent.VK_O);
			//TODO Show the number of lines written
			// Add the components
			setLayout(new BorderLayout());
			add(tb, BorderLayout.CENTER);
		}

		@Override
		public String getPanelName() {
			return "Process Info";
		}

		@Override
		public void pluginPanelRemoved() {
			// Nothing to do i think
		}

		@Override
		public Dimension getInitialSize() {
			return getPreferredSize();
		}
	}
}
