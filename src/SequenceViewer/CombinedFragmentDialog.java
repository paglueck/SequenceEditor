package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Message;


@SuppressWarnings("serial")
public class CombinedFragmentDialog extends JFrame {	
	
    // Variables declaration
	private JLabel lblName;
	private JLabel lblType;
	private JLabel lblConstraint;
	private JLabel lblLifelines;
	private JLabel lblInsert;
	
	private JComboBox<String> cmbType;
	private JComboBox<String> cmbInsert;
	
	private JTextField txtName;
	private JTextField txtConstraint;
	
	private JList<String> lstLifelines;
	
	private JButton btnAccept;
	private JButton btnCancel;
	
	private JScrollPane scrllList;
	
	private JPanel contentPane;

	private Lifeline[] lifelines;
	private EList<Lifeline> lifelinelist;
	
	private Interaction interaction;
	private CombinedFragment editFragment;
	private ModelUtilities.DialogOps op;
	// End of variables declaration    


    public CombinedFragmentDialog(Interaction inter, ModelUtilities.DialogOps operation) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.setWindowProperties("Create New Combined Fragment");
        this.create();
        this.setVisible(true);
    }    
    
    public CombinedFragmentDialog(Interaction inter, ModelUtilities.DialogOps operation, CombinedFragment editFragment) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.editFragment = editFragment;
        this.setWindowProperties("Edit Combined Fragment");
        this.create();
        this.setEditProperties();
        this.setVisible(true);
    }    

    private void create() {
    	lifelinelist = interaction.getLifelines();
    	lifelines = (Lifeline[]) lifelinelist.toArray();
    	
    	String[] strLifelines = ModelUtilities.lifelinesToStrings(lifelines);
    	String[] strTypes = new String[]{"Alt", "Opt", "Par", "Critical", "Loop"};
    	EList<Message> messages = interaction.getMessages();
    	List<Message> filteredMessages = ModelUtilities.getRelevantMessages(messages, lifelines[0]); 

    	String[] strMessages = ModelUtilities.messagesToStrings(filteredMessages.toArray()); 
        this.initializeComponents(strLifelines, strTypes, strMessages);
        
    	lstLifelines.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	lstLifelines.setLayoutOrientation(JList.VERTICAL);
    	lstLifelines.setVisibleRowCount(2);
    	
    	scrllList = new JScrollPane(lstLifelines);
    	scrllList.setPreferredSize(new Dimension(250, 100));
    	
    	this.setLabels();
    	this.addComponents();
    	this.setLayout();
    	this.setActionListeners();
    }
    
    private void initializeComponents(String[] strLifelines, String[] strTypes, String[] strMessages) {
    	lblName = new JLabel("CF Name:");
    	lblType = new JLabel("Type:");
    	lblConstraint = new JLabel("Constraint:");
    	lblInsert = new JLabel("Insert After/Before: ");
    	lblLifelines = new JLabel("Lifeline Coverage:");
    	
    	txtName = new JTextField(1);
    	txtConstraint = new JTextField(1);
    	
    	cmbType = new JComboBox<String>(strTypes);
    	cmbInsert = new JComboBox<String>(strMessages);
    	
    	lstLifelines = new JList<String>(strLifelines);
    	
    	btnAccept = new JButton("Ok");
    	btnCancel = new JButton("Cancel");
    	
    	contentPane = (JPanel) this.getContentPane();
    }
    
    private void addComponents() {
        contentPane.add(lblName);
        contentPane.add(txtName);
    	contentPane.add(lblType);
    	contentPane.add(cmbType);
    	contentPane.add(lblConstraint);
    	contentPane.add(txtConstraint);
    	contentPane.add(lblInsert);
    	contentPane.add(cmbInsert);
    	contentPane.add(lblLifelines);
    	contentPane.add(scrllList);
    	contentPane.add(btnAccept);
    	contentPane.add(btnCancel);
    }
    
    private void setLabels() {
        lblName.setLabelFor(txtName);
    	lblType.setLabelFor(cmbType);
    	lblConstraint.setLabelFor(txtConstraint);
    	lblInsert.setLabelFor(cmbInsert);
    	lblLifelines.setLabelFor(scrllList);    	
    }
    
    private void setLayout() {
    	SpringLayout layout = new SpringLayout();
    	contentPane.setLayout(layout);
    	SpringUtilities.makeCompactGrid(contentPane, 6, 2,  //rows, cols
    												 6, 3,  //initX, initY
												 	 6, 3); //xPad, yPad
    }
    
    public void setWindowProperties(String title) {
    	this.setTitle(title);
    	this.setSize(new Dimension(400, 300));
    	this.setLocation(ModelUtilities.getCenterPosition(this));
    	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	this.setResizable(false);
    }
    
    public void setEditProperties() {
    	this.txtName.setText(this.editFragment.getName());
    	this.cmbType.setSelectedItem(this.editFragment.getInteractionOperator().getName());
    	//this.txtConstraint.setText(((LiteralString)this.editFragment.getOperand(this.editFragment.getName()+"Operand").getGuard()).getValue());
    	EList<Lifeline> templist = this.editFragment.getCovereds();
    	int[] indices = new int[templist.size()];
    	int i = 0;
    	for (Lifeline l : templist) {
    		indices[i] = this.lifelinelist.indexOf(l);
    		i++;
    	}
    	this.lstLifelines.setSelectedIndices(indices);
    }
    
    private void setActionListeners() {
		btnAccept.addActionListener(new ActionListener() {

    			@Override
    			public void actionPerformed(ActionEvent e) {			
    				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
    		        domain.getCommandStack().execute(new RecordingCommand(domain) {

    		            @Override
    		            protected void doExecute() {    
    		            	if (op == ModelUtilities.DialogOps.EDIT) {
    		            		ModelOperations.createCombinedFragment(interaction, txtName.getText(), ModelUtilities.getInteractionOperatorKind((String)cmbType.getSelectedItem()), ModelUtilities.getSelectedLifelines(lifelines, lstLifelines.getSelectedIndices()), txtConstraint.getText());
    		            	} else {
    		            		ModelOperations.createCombinedFragment(interaction, txtName.getText(), ModelUtilities.getInteractionOperatorKind((String)cmbType.getSelectedItem()), ModelUtilities.getSelectedLifelines(lifelines, lstLifelines.getSelectedIndices()), txtConstraint.getText());
    		            	}
    		            	
    		            	
    		            }
    		        });			
    		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();
    			}
    			
    		});
    		
		btnCancel.addActionListener(ModelUtilities.getCancelActionListener());	
    }
   
}