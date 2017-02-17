package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.*;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;


@SuppressWarnings("serial")
public class MessageDialog extends JFrame {
	
    // Variables declaration
	private JLabel lblName;
	private JLabel lblOriginLifeline;
	private JLabel lblDestinationLifeline;
	private JLabel lblMessageSort;
	private JLabel lblMessageKind;
	private JLabel lblInsert;
	private JLabel lblAfterBefore;
	private JLabel lblSignature;
	private JLabel lblParameters;
	private JLabel lblReturns;
	
	private JComboBox<String> cmbOriginLifeline;
	private JComboBox<String> cmbDestinationLifeline;
	private JComboBox<String> cmbMessageSort;
	private JComboBox<String> cmbMessageKind;
	private JComboBox<String> cmbInsert;
	private JComboBox<String> cmbAfterBefore;
	
	private JTextField txtName;
	private JTextField txtParameters;
	private JTextField txtReturns;
	
	private JButton btnAccept;
	private JButton btnCancel;
	
	private JCheckBox chSignature;
	
	private JPanel contentPane;	

	private MessageOccurrenceSpecification insert;
	
	private Interaction interaction;
	
	private EList<Message> messages;
	
	private Lifeline[] lifelines;
	private ModelUtilities.DialogOps op;
	private Message editMessage;
	List<Message> filteredMessages;
	// End of variables declaration    


    public MessageDialog(Interaction inter, ModelUtilities.DialogOps op) {
        super();
        this.interaction = inter;
        this.setWindowProperties("Create New Message");
        this.create();
        this.setVisible(true);
    }    
    
    public MessageDialog(Interaction inter, ModelUtilities.DialogOps op, Message editMessage) {
        super();
        this.interaction = inter;
        this.op = op;
        this.editMessage = editMessage;
        this.setWindowProperties("Edit Message");
        this.create();
        this.setEditProperties();
        this.setVisible(true);
    }    

    private void create() {  	
    	
    	lifelines = (Lifeline[]) interaction.getLifelines().toArray();
    	String[] strLifelines = ModelUtilities.lifelinesToStrings(lifelines);  
    	String[] strMessageSort = ModelUtilities.valuesToStrings(MessageSort.values());
    	String[] strMessageKind = ModelUtilities.valuesToStrings(MessageKind.values());
    	this.messages = interaction.getMessages();
    	filteredMessages = ModelUtilities.getRelevantMessages(messages, lifelines[0]);    	
    	
    	String[] strMessages = ModelUtilities.messagesToStrings(filteredMessages.toArray()); 
        this.initializeComponents(strLifelines, strMessageSort, strMessageKind, strMessages);
    	SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
    	this.setLabels();
    	this.addComponents();
    	SpringUtilities.makeCompactGrid(contentPane,
    	                                11, 2, //rows, cols
    	                                6, 3,        //initX, initY
    	                                6, 3);       //xPad, yPad
    	
    	btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {			
				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
		        domain.getCommandStack().execute(new RecordingCommand(domain) {

		            @Override
		            protected void doExecute() { 
		            	Message selMsg = filteredMessages.get(cmbInsert.getSelectedIndex());
		            	MessageOccurrenceSpecification sendE = (MessageOccurrenceSpecification) selMsg.getSendEvent();
		            	MessageOccurrenceSpecification recvE = (MessageOccurrenceSpecification) selMsg.getReceiveEvent();
		        		insert = sendE.getCovered() == lifelines[cmbOriginLifeline.getSelectedIndex()] ? sendE : recvE;	        		
		        		ModelUtilities.InsertPoint insertPoint = ((String)cmbAfterBefore.getSelectedItem()).equals("After") ? ModelUtilities.InsertPoint.AFTER : ModelUtilities.InsertPoint.BEFORE; 
		        		
		        		if (op == ModelUtilities.DialogOps.EDIT) {
		        		} else {
			            	ModelOperations.createMessage2(interaction, txtName.getText(), lifelines[cmbOriginLifeline.getSelectedIndex()], 
			            			lifelines[cmbDestinationLifeline.getSelectedIndex()], MessageSort.values()[cmbMessageSort.getSelectedIndex()], MessageKind.values()[cmbMessageKind.getSelectedIndex()], insert, insertPoint, ModelUtilities.getParameters(txtParameters.getText()), ModelUtilities.getReturn(txtReturns.getText()));
		        		}
		        	}
		        });			
		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();
			}
    		
    	});
    	btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    	cmbOriginLifeline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setInsertBox(lifelines[cmbOriginLifeline.getSelectedIndex()]);
			}    		
    	});
    	chSignature.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (txtParameters.isEnabled() && txtReturns.isEnabled()) {
					txtParameters.setText("");
					txtReturns.setText("");
					txtParameters.setEnabled(false);
					txtReturns.setEnabled(false);	
					lblParameters.setEnabled(false);
					lblReturns.setEnabled(false);
				} else {
					txtParameters.setEnabled(true);
					txtReturns.setEnabled(true);	
					lblParameters.setEnabled(true);
					lblReturns.setEnabled(true);
				}
			}    		
    	});
    	cmbInsert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Lifeline l = lifelines[cmbOriginLifeline.getSelectedIndex()];
				Message m = filteredMessages.get(cmbInsert.getSelectedIndex());
				MessageOccurrenceSpecification mos = ((MessageOccurrenceSpecification) m.getSendEvent()).getCovered() == l ? (MessageOccurrenceSpecification)m.getSendEvent() : (MessageOccurrenceSpecification)m.getReceiveEvent();
				ActionExecutionSpecification soe = ModelUtilities.isExecutionStartOrEnd(l, mos);
				System.out.println(mos);
				System.out.println(soe);
				if (soe != null) {
					lblAfterBefore.setEnabled(true);
					cmbAfterBefore.setEnabled(true);
				} else {
					lblAfterBefore.setEnabled(false);
					cmbAfterBefore.setEnabled(false);
				}
				
			}
    		
    	});
    }
    
    private void initializeComponents(String[] strLifelines, String[] strMessageSort, String[]strMessageKind, String[] strMessages) {
    	lblName = new JLabel("Message Name:");
    	lblOriginLifeline = new JLabel("Origin Lifeline:");
    	lblDestinationLifeline = new JLabel("Destination Lifeline:");
    	lblMessageSort = new JLabel("Message Sort:");
    	lblMessageKind = new JLabel("Message Kind:");
    	lblInsert = new JLabel("Insert After/Before:");
    	lblAfterBefore = new JLabel("");
    	lblSignature = new JLabel("Without Signature:");
    	lblParameters = new JLabel("Parameters:");
    	lblReturns = new JLabel("Return:");
    	
    	txtName = new JTextField();
    	txtParameters = new JTextField();
    	txtReturns = new JTextField();
    	
    	cmbOriginLifeline = new JComboBox<String>(strLifelines);
    	cmbDestinationLifeline = new JComboBox<String>(strLifelines);
    	cmbMessageSort = new JComboBox<String>(strMessageSort);
    	cmbMessageKind = new JComboBox<String>(strMessageKind);
    	cmbInsert = new JComboBox<String>(strMessages);
    	cmbAfterBefore = new JComboBox<String>(new String[]{"After", "Before"});
    	
    	chSignature = new JCheckBox();
    	
    	btnAccept = new JButton("Ok");
    	btnCancel = new JButton("Cancel");
    	    	
    	contentPane = (JPanel) this.getContentPane();
    }
    
    private void addComponents() {
        contentPane.add(lblName);
        contentPane.add(txtName);
    	contentPane.add(lblOriginLifeline);
    	contentPane.add(cmbOriginLifeline);
    	contentPane.add(lblDestinationLifeline);
    	contentPane.add(cmbDestinationLifeline);
    	contentPane.add(lblMessageSort);
    	contentPane.add(cmbMessageSort);
    	contentPane.add(lblMessageKind);
    	contentPane.add(cmbMessageKind);
    	contentPane.add(lblInsert);
    	contentPane.add(cmbInsert);
    	contentPane.add(lblAfterBefore);
    	contentPane.add(cmbAfterBefore);
    	contentPane.add(lblSignature);
    	contentPane.add(chSignature);
    	contentPane.add(lblParameters);
    	contentPane.add(txtParameters);
    	contentPane.add(lblReturns);
    	contentPane.add(txtReturns);
    	contentPane.add(btnAccept);
    	contentPane.add(btnCancel);
    }
    
    private void setLabels() {
        lblName.setLabelFor(txtName);
    	lblOriginLifeline.setLabelFor(cmbOriginLifeline);
    	lblOriginLifeline.setLabelFor(cmbDestinationLifeline);
    	lblMessageSort.setLabelFor(cmbMessageSort);
    	lblMessageKind.setLabelFor(cmbMessageKind);
    	lblInsert.setLabelFor(cmbInsert);
    	lblAfterBefore.setLabelFor(cmbAfterBefore);
    	lblSignature.setLabelFor(chSignature);
    	lblParameters.setLabelFor(txtParameters);
    	lblReturns.setLabelFor(txtReturns);    	
    }
    
    private void setWindowProperties(String title) {
    	this.setTitle(title);
    	this.setSize(new Dimension(400, 390));
    	this.setLocation(ModelUtilities.getCenterPosition(this));
    	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	this.setResizable(false);
    }
    
    private void setEditProperties() {
    	this.txtName.setText(this.editMessage.getName());
    	this.cmbOriginLifeline.setEnabled(false);
    	this.cmbDestinationLifeline.setEnabled(false);
    	this.lblOriginLifeline.setEnabled(false);
    	this.lblDestinationLifeline.setEnabled(false);
    	this.cmbMessageSort.setSelectedItem(this.editMessage.getMessageSort().getName());
    	this.cmbMessageKind.setSelectedItem(this.editMessage.getMessageKind().getName());
    	this.setInsertBox(((MessageOccurrenceSpecification)this.editMessage.getSendEvent()).getCovered());
    }
 
    private void setInsertBox(Lifeline l) {
    	this.filteredMessages = ModelUtilities.getRelevantMessages(this.messages, l);
		DefaultComboBoxModel model = new DefaultComboBoxModel(ModelUtilities.messagesToStrings(this.filteredMessages.toArray()));				
		cmbInsert.setModel(model);	
    }
    
}