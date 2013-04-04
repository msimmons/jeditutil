package net.contrapt.jeditutil;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.Component;

/**
* This property change listener listens for focus change and changes the border color of the
* parent component when focus changes
*/
public class FocusPropertyChangeListener implements PropertyChangeListener {
	
	private JComponent component;
	private Component lastFocusedComponent;
	private static Color unfocusColor = Color.GRAY;
	private static Color focusColor = Color.BLUE;
	private static LineBorder unfocusBorder = (LineBorder)BorderFactory.createLineBorder(unfocusColor);
	private static LineBorder focusBorder = (LineBorder)BorderFactory.createLineBorder(focusColor, 2);

	FocusPropertyChangeListener(JComponent c) {
		this.component = c;
		component.setBorder(unfocusBorder);
		lastFocusedComponent = c;
	}

	/**
	* Return the last focused component
	*/
	public Component getLastFocusedComponent() {
		return lastFocusedComponent;
	}
	
	/**
	* When any sub-component of this component gets or looses focus, switch the border color to indicate
	* the focus change
	*/
	public void propertyChange(PropertyChangeEvent e) {
		boolean hadFocus = ((LineBorder)component.getBorder()).getLineColor().equals(focusColor);
		Component c = (Component)e.getNewValue();
		lastFocusedComponent = c;
		boolean hasFocus = false;
		while ( c != null ) {
			if ( c.equals(component) ) {
				hasFocus = true;
				break;
			}
			c = c.getParent();
		}
		if ( hasFocus && !hadFocus ) component.setBorder(focusBorder);
		else if ( hadFocus ) component.setBorder(unfocusBorder);
	}

}

