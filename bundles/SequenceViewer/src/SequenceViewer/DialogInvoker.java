package SequenceViewer;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Model;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JTextField;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.action.AbstractExternalJavaAction;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;

import javax.imageio.ImageIO;
import javax.swing.*;
public class DialogInvoker extends AbstractExternalJavaAction implements IExternalJavaAction {
	
	@Override
	public boolean canExecute(Collection<? extends EObject> args) {
		return true;
	}
	
	private DialogInputServices dialogInputServices = new DialogInputServices();
	
	@Override
	public void execute(Collection<? extends EObject> args, Map<String,	Object> arg1) {
		String s = (String) arg1.get("type");
		Interaction t = (Interaction) arg1.get("interaction");
		System.out.println("Class: " + t.eClass().getName());
		System.out.println("Name: " + t.getName());
		System.out.println(s);
		Lifeline l1 = t.getLifeline("AddMoviesLifeline");
		Lifeline l2 = t.getLifeline("MainFrameLifeline");
		if (s.equals("'newLL'")) {
			CreateLifelineDialog cld = new CreateLifelineDialog(t);
		} else if (s.equals("'newNM'")) {
			CreateMessageDialog cmd = new CreateMessageDialog(t);
		} else if (s.equals("'newSI'")) {
			CreateInvariantDialog cid = new CreateInvariantDialog(t);
		}
			
			
		
	}
	
	

}

