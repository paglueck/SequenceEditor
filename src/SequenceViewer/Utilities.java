package SequenceViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.eclipse.uml2.uml.Lifeline;

public class Utilities {
	 
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
    
    public static ActionListener getCancelActionListener() {
    	return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();;
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
}
