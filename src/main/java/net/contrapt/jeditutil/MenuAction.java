package net.contrapt.jeditutil;

import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.ActionSet;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import net.contrapt.jeditutil.model.MenuDef;

import java.util.Collection;

/**
 * An implementation of <code>EditAction</code> which dynamically creates
 * actions for global menu definitions
 */
public class MenuAction extends EditAction {

	private static final String NAME_PREFIX = "jeditutil-menu";
	private static ActionSet actionSet;

	private String menu;

	/**
	 * Construct an action for the given menu
	 */
	public MenuAction(String menu) {
		super(NAME_PREFIX + "-" + menu);
		this.menu = menu;
	}

	/**
	 * Return the beanshell code to execute this menu
	 */
	@Override
	public String getCode() {
		return "net.contrapt.jeditutil.UtilPlugin.getInstance().showGlobalMenu(view,\""
				+ menu + "\");";
	}

	/**
	 * Return a label for this action
	 */
	@Override
	public String getLabel() {
		return getName();
	}

	/**
	 * Invoke this tool
	 */
	@Override
	public void invoke(View view) {
		UtilPlugin.getInstance().showGlobalMenu(view, menu);
	}

	/**
	 * Initialize an action set with any defined global menus
	 */
	public static void initialize(Collection<MenuDef> menus) {
		actionSet = new ActionSet("Global Menus");
		for (MenuDef menu : menus) {
			MenuAction action = new MenuAction(menu.getName());
			actionSet.addAction(action);
		}
		jEdit.addActionSet(actionSet);
		actionSet.initKeyBindings();
	}

	/**
	 * Unregister the action set
	 */
	public static void shutdown() {
		if (actionSet != null)
			jEdit.removeActionSet(actionSet);
		actionSet = null;
	}

}
