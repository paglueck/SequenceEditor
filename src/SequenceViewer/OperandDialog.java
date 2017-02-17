package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;


@SuppressWarnings("serial")
public class OperandDialog extends JFrame {
	
    // Variables declaration
	private JLabel lblConstraint;
	
	private JTextField txtConstraint;
	private JButton btnAccept;
	private JButton btnCancel;
	private JPanel contentPane;

	private Interaction interaction;
	private CombinedFragment comFragment;
	private ModelUtilities.DialogOps op;
	// End of variables declaration    


    public OperandDialog(Interaction inter, ModelUtilities.DialogOps operation, CombinedFragment cf) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.comFragment = cf;
        this.setWindowProperties("Edit InteractionOperands");
        create();
        this.setVisible(true);
    }    
    
    public OperandDialog(CombinedFragment cf, ModelUtilities.DialogOps operation) {
        super();
        this.interaction = cf.getEnclosingInteraction();
        this.comFragment = cf;
        this.op = operation;
        this.setWindowProperties("Add New InteractionOperand");
        create();
        this.setVisible(true);
    }    

    private void create() { 	
        this.initializeComponents();    	
        this.setLabels();
    	this.addComponents();
    	if (this.op == ModelUtilities.DialogOps.EDIT) {    		
    		this.setEditProperties();
    	}
    	SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
    	
    	SpringUtilities.makeCompactGrid(contentPane,
    	                                2, 2, //rows, cols
    	                                6, 3,        //initX, initY
    	                                6, 3);       //xPad, yPad
    	
    	btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {			
				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
		        domain.getCommandStack().execute(new RecordingCommand(domain) {

		            @Override
		            protected void doExecute() {
		            	ModelOperations.createInteractionOperand(comFragment, txtConstraint.getText());
		            }
		        });			
		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();
			}
    		
    	});
    	btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    }
    
    public void setWindowProperties(String title) {
    	this.setTitle(title);
    	this.setLocation(ModelUtilities.getCenterPosition(this));
    	this.setSize(new Dimension(400, 90));
    	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	this.setResizable(false);
    }
    
    public void initializeComponents() {
    	lblConstraint = new JLabel("Constraint:"); 	
    	
    	txtConstraint = new JTextField();    	
    	
    	btnAccept = new JButton("Ok");
    	btnCancel = new JButton("Cancel");
    	contentPane = (JPanel) this.getContentPane();
    }
    
    public void addComponents() {
    	contentPane.add(lblConstraint);
    	contentPane.add(txtConstraint);
    	contentPane.add(btnAccept);
    	contentPane.add(btnCancel);
    }
    
    public void setLabels() {
    	lblConstraint.setLabelFor(txtConstraint);    
    }
    
    public void setEditProperties() {
    
    }
}