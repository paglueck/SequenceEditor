package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;


@SuppressWarnings("serial")
public class CreateLifelineDialog extends JFrame {
	
    // Variables declaration
	private JLabel lblName;
	private JLabel lblClass;
	private JLabel lblInsert;
	
	private JComboBox<String> cmbClass;
	private JComboBox<String> cmbInsert;
	private JTextField txtName;
	private JButton btnAccept;
	private JButton btnCancel;
	private JPanel contentPane;

	private Interaction interaction;
	// End of variables declaration    


    public CreateLifelineDialog(Interaction inter) {
        super();
        this.interaction = inter;
        create();
        this.setVisible(true);
    }    

    private void create() {
    	this.setTitle("Create New StateInvariant");
    	this.setLocation(new Point(500,500));
    	this.setSize(new Dimension(400, 150));
    	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	this.setResizable(false);
    	EList<PackageableElement> pes = interaction.getModel().getPackagedElements();
    	List<PackageableElement> ps = new ArrayList<PackageableElement>();
    	for(PackageableElement el : pes) {
    		if ((el.eClass().getName().equals("Class"))) {
    			ps.add(el);
    		}
    	}
    	
    	String[] strClasses = new String[ps.size()];
    	for(int i = 0; i < ps.size(); i++) {
    		strClasses[i] = ps.get(i).getName();
    	}
    	
    	EList<Lifeline> lifelines = interaction.getLifelines();
    	String[] strLifelines = Utilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());  
    	
        this.initializeComponents(strClasses, Utilities.concat(new String[]{"Start"}, strLifelines));
    	SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
    	this.setLabels();
    	this.addComponents();
    	SpringUtilities.makeCompactGrid(contentPane,
    	                                3, 2, //rows, cols
    	                                6, 3,        //initX, initY
    	                                6, 3);       //xPad, yPad
    	
    	btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {			
				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
		        domain.getCommandStack().execute(new RecordingCommand(domain) {

		            @Override
		            protected void doExecute() {                
		            	ModelOperations.createLifeline(interaction, txtName.getText(), (org.eclipse.uml2.uml.Class) ps.get(cmbClass.getSelectedIndex()), (String)cmbInsert.getSelectedItem());
		            }
		        });			
		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();;
			}
    		
    	});
    	btnCancel.addActionListener(Utilities.getCancelActionListener());
    }
    
    public void initializeComponents(String[] classes, String[] lifelines) {
    	lblName = new JLabel("Lifeline Instance Name: ");
    	lblClass = new JLabel("Class: ");
    	lblInsert = new JLabel("Insert After: ");
    	txtName = new JTextField(3);
    	cmbClass = new JComboBox<String>(classes);
    	cmbInsert = new JComboBox<String>(lifelines);
    	btnAccept = new JButton("Ok");
    	btnCancel = new JButton("Cancel");
    	contentPane = (JPanel) this.getContentPane();
    }
    
    public void addComponents() {
        contentPane.add(lblName);
        contentPane.add(txtName);
    	contentPane.add(lblClass);
    	contentPane.add(cmbClass);
    	contentPane.add(lblInsert);
    	contentPane.add(cmbInsert);
    	contentPane.add(btnAccept);
    	contentPane.add(btnCancel);
    }
    
    public void setLabels() {
        lblName.setLabelFor(txtName);
    	lblClass.setLabelFor(cmbClass);    
    	lblInsert.setLabelFor(cmbInsert);
    }
}