package SequenceViewer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

@SuppressWarnings("serial")
public class CustomNameRenderer extends DefaultListCellRenderer {
	
	    @Override
	    public Component getListCellRendererComponent(JList<?> list,
	                                                  Object value,
	                                                  int index,
	                                                  boolean isSelected,
	                                                  boolean cellHasFocus) {
	        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        setText(((NamedElement) value).getName());
	        return this;
	    }
	

}
