package SequenceViewer;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class DialogInputServices {

public String stringInput(String title, String prompt, String initial, String def, String pattern) {
	InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), title, prompt, initial, new RegexValidator(pattern));
	return (dialog.open() == Window.OK ? dialog.getValue() : def);
}

public String stringInput(String title, String prompt) {
	return stringInput(title, prompt, null, null, null);
}

public String stringInput(String prompt) {
	return stringInput("Input String", prompt);
}

private class RegexValidator implements IInputValidator {

	private final String pattern;
	
		public RegexValidator(String pattern) {
			this.pattern = pattern;
		}
		
		@Override
		public String isValid(String newText) {
			return (pattern == null || newText.matches(pattern) ? null : "Input es not match " + pattern);
		}
	}
}
