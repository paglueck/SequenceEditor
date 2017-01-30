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
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageSort;


@SuppressWarnings("serial")
public class CreateMessageDialog extends JFrame {
	
    // Variables declaration
	private JLabel lblName;
	private JLabel lblOriginLifeline;
	private JLabel lblDestinationLifeline;
	private JLabel lblMessageSort;
	private JLabel lblMessageKind;
	private JLabel lblParameters;
	private JLabel lblReturns;
	
	private JComboBox<String> cmbOriginLifeline;
	private JComboBox<String> cmbDestinationLifeline;
	private JComboBox<String> cmbMessageSort;
	private JComboBox<String> cmbMessageKind;
	private JTextField txtName;
	private JTextField txtParameters;
	private JTextField txtReturns;
	private JButton btnAccept;
	private JButton btnCancel;
	private JPanel contentPane;

	private Interaction interaction;
	// End of variables declaration    


    public CreateMessageDialog(Interaction inter) {
        super();
        this.interaction = inter;
        create();
        this.setVisible(true);
    }    

    private void create() {
    	this.setTitle("Create New Message");
    	this.setLocation(new Point(500,500));
    	this.setSize(new Dimension(400, 270));
    	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    	this.setResizable(false);
    	
    	EList<Lifeline> lifelines = interaction.getLifelines();
    	String[] strLifelines = Utilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());  
    	String[] strMessageSort = Utilities.valuesToStrings(MessageSort.values());
    	String[] strMessageKind = Utilities.valuesToStrings(MessageKind.values());

        this.initializeComponents(strLifelines, strMessageSort, strMessageKind);
    	SpringLayout layout = new SpringLayout();
        contentPane.setLayout(layout);
    	this.setLabels();
    	this.addComponents();
    	SpringUtilities.makeCompactGrid(contentPane,
    	                                8, 2, //rows, cols
    	                                6, 3,        //initX, initY
    	                                6, 3);       //xPad, yPad
    	
    	btnAccept.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {			
				TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(interaction);
		        domain.getCommandStack().execute(new RecordingCommand(domain) {

		            @Override
		            protected void doExecute() {                
		            	ModelOperations.createMessage(interaction, txtName.getText(), lifelines.get(cmbOriginLifeline.getSelectedIndex()), 
		            			lifelines.get(cmbDestinationLifeline.getSelectedIndex()), MessageSort.values()[cmbMessageSort.getSelectedIndex()], MessageKind.values()[cmbMessageKind.getSelectedIndex()]);
		            }
		        });			
		        SwingUtilities.getWindowAncestor((JButton)e.getSource()).dispose();;
			}
    		
    	});
    	btnCancel.addActionListener(Utilities.getCancelActionListener());
    }
    
    private void initializeComponents(String[] strLifelines, String[] strMessageSort, String[]strMessageKind) {
    	lblName = new JLabel("Message Name: ");
    	lblOriginLifeline = new JLabel("Origin Lifeline:");
    	lblDestinationLifeline = new JLabel("Destination Lifeline:");
    	lblMessageSort = new JLabel("Message Sort:");
    	lblMessageKind = new JLabel("Message Kind: ");
    	lblParameters = new JLabel("Parameters: ");
    	lblReturns = new JLabel("Return: ");
    	txtName = new JTextField(3);
    	txtParameters = new JTextField(3);
    	txtReturns = new JTextField(3);
    	cmbOriginLifeline = new JComboBox<String>(strLifelines);
    	cmbDestinationLifeline = new JComboBox<String>(strLifelines);
    	cmbMessageSort = new JComboBox<String>(strMessageSort);
    	cmbMessageKind = new JComboBox<String>(strMessageKind);
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
    	lblParameters.setLabelFor(txtParameters);
    	lblReturns.setLabelFor(txtReturns);    	
    }
   
}