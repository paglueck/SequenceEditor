package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;


@SuppressWarnings("serial")
public class CreateInvariantDialog extends JFrame {
	
    // Variables declaration
	private JLabel lblName;
	private JLabel lblLifeline;
	private JLabel lblConstraint;
	
	private JComboBox<String> cmbLifeline;
	private JTextField txtName;
	private JTextField txtConstraint;
	private JButton btnAccept;
	private JButton btnCancel;
	private JPanel contentPane;

	private Interaction interaction;
	// End of variables declaration    


    public CreateInvariantDialog(Interaction inter) {
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
    	
    	EList<Lifeline> lifelines = interaction.getLifelines();
    	String[] strLifelines = Utilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());

        this.initializeComponents(strLifelines);
    	SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
    	this.setLabels();
    	this.addComponents();
    	SpringUtilities.makeCompactGrid(contentPane,
    	                                4, 2, //rows, cols
    	                                6, 3,        //initX, initY
    	                                6, 3);       //xPad, yPad
    	
    	btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {			
				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
		        domain.getCommandStack().execute(new RecordingCommand(domain) {

		            @Override
		            protected void doExecute() {                
		            	ModelOperations.createStateInvariant(interaction, txtName.getText(), lifelines.get(cmbLifeline.getSelectedIndex()), txtConstraint.getText());
		            }
		        });			
		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();;
			}
    		
    	});
    	btnCancel.addActionListener(Utilities.getCancelActionListener());
    }
    
    public void initializeComponents(String[] strLifelines) {
    	lblName = new JLabel("StateInvariant Name: ");
    	lblLifeline = new JLabel("For Lifeline:");
    	lblConstraint = new JLabel("Constraint: ");
    	txtName = new JTextField(3);
    	txtConstraint = new JTextField(3);
    	cmbLifeline = new JComboBox<String>(strLifelines);
    	btnAccept = new JButton("Ok");
    	btnCancel = new JButton("Cancel");
    	contentPane = (JPanel) this.getContentPane();
    }
    
    public void addComponents() {
        contentPane.add(lblName);
        contentPane.add(txtName);
    	contentPane.add(lblLifeline);
    	contentPane.add(cmbLifeline);
    	contentPane.add(lblConstraint);
    	contentPane.add(txtConstraint);
    	contentPane.add(btnAccept);
    	contentPane.add(btnCancel);
    }
    
    public void setLabels() {
        lblName.setLabelFor(txtName);
    	lblLifeline.setLabelFor(cmbLifeline);
    	lblConstraint.setLabelFor(txtConstraint);    	
    }
}