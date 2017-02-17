package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

public class ModelUtilities {
	 
	public enum DialogOps {
		CREATE, EDIT
	}
	
	public enum InsertPoint {
		AFTER, BEFORE
	}
	
    public static String[] valuesToStrings(Object[] values) {
    	String[] strings = new String[values.length];
    	for (int i = 0; i < values.length; i++) {
    		strings[i] = values[i].toString();
    	}
    	return strings;
    }
    
    public static String[] lifelinesToStrings(Lifeline[] lifelines) {
    	String[] strLifelines = new String[lifelines.length];
    	for (int i = 0; i < lifelines.length; i++) {
    		strLifelines[i] = lifelines[i].getName();
    	}
    	return strLifelines;
    }
    
    public static String[] messagesToStrings(Object[] messages) {
    	String[] strMessages = new String[messages.length];
    	for (int i = 0; i < messages.length; i++) {
    		Message m = (Message) messages[i];
    		strMessages[i] = m.getName();
    	}
    	return strMessages;
    }
    
    public static ActionListener getCancelActionListener() {
    	return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();
			}
		};
    }
    
    public static String[] concat(String[] a, String[] b) {
    	int aLen = a.length;
    	int bLen = b.length;
    	String[] c = new String[aLen+bLen];
    	System.arraycopy(a, 0, c, 0, aLen);
    	System.arraycopy(b, 0, c, aLen, bLen);
    	return c;    		
    }
    
    public static Point getCenterPosition(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        return new Point(x,y);
    }
    
    public static InteractionOperatorKind getInteractionOperatorKind(String iop) {
    	switch (iop) {
    		case "Alt": return InteractionOperatorKind.ALT_LITERAL;
    		case "Opt": return InteractionOperatorKind.OPT_LITERAL;
    		case "Par": return InteractionOperatorKind.PAR_LITERAL;
    		case "Crit": return InteractionOperatorKind.CRITICAL_LITERAL;
    		case "Loop": return InteractionOperatorKind.LOOP_LITERAL;
    		default: return InteractionOperatorKind.ALT_LITERAL;
    	}
    }
    
    public static Lifeline[] getSelectedLifelines(Lifeline[] allLifelines, int[] selections) {
    	Lifeline[] lifelines = new Lifeline[selections.length];
    	for (int i = 0; i < selections.length; i++) {
    		lifelines[i] = allLifelines[selections[i]];
    	}
    	return lifelines;
    }
    
    public static List<Message> getRelevantMessages(EList<Message> messages, Lifeline lifeline) {
    	List<Message> filteredMessages = new ArrayList<Message>();
    	for (Message m : messages) {
    		MessageOccurrenceSpecification smos = (MessageOccurrenceSpecification) m.getSendEvent();
    		MessageOccurrenceSpecification rmos = (MessageOccurrenceSpecification) m.getReceiveEvent();
    		if(smos.getCovered() == lifeline || rmos.getCovered() == lifeline) {
    			filteredMessages.add(m);
    		}
    	}
    	return filteredMessages;
    }
    
    public static LinkedHashMap<String, String> getParameters(String paramString) {
    	LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
    	String[] paramPairs = paramString.split(";");
    	for(String p : paramPairs) {
    		String[] s = p.split(":");
    		if (s.length == 2) {
    			parameters.put(s[0], s[1]);
    		} else {
    			return null;
    		}
    	}
    	return parameters;
    }
    
    public static String[] getReturn(String returnString) {
    	return returnString.split(":");
    }
    
    public static boolean isPrimitiveType(String check) {
    	switch (check) {
	    	case "String": return true;
	    	case "Boolean": return true;
	    	case "Integer": return true;
	    	default: return false;
    	}
    }
    
    public static ActionExecutionSpecification isExecutionStartOrEnd(Lifeline l, MessageOccurrenceSpecification mos) {
    	EList<InteractionFragment> lineFragments = l.getCoveredBys();
    	for (InteractionFragment f : lineFragments) {
    		if (f.eClass() == UMLPackage.eINSTANCE.getActionExecutionSpecification()) {
    			if (((ActionExecutionSpecification)f).getStart() == mos || ((ActionExecutionSpecification)f).getFinish() == mos) {
    				return (ActionExecutionSpecification) f;
    			}
    		}
    	}
    	return null;
    }
}
