package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

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

import SequenceViewer.ModelUtilities.DialogOps;

/**
 * Class providing a dialog window for creating and editing a {@link Message}.
 */
public class MessageDialog extends JFrame {

    private static final long serialVersionUID = 1L;
    
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

    private final Interaction interaction;

    private EList<Message> messages;

    private Lifeline[] lifelines;
    private ModelUtilities.DialogOps op;
    private Message editMessage;
    List<Message> filteredMessages;
    // End of variables declaration

    /**
     * Constructor for creating a dialog for creating a new {@link Message}.
     * 
     * @param inter
     *            the interaction holding the containment for the new {@link Message}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether a {@link Message} gets created or
     *            edited, not <code>null</code>
     */
    public MessageDialog(final Interaction inter, final ModelUtilities.DialogOps op) {
        super();
        this.interaction = inter;
        this.setWindowProperties("Create New Message");
        this.initialize();
        this.setVisible(true);
    }

    /**
     * Constructor for creating a dialog for editing an existing {@link Message}.
     * 
     * @param inter
     *            the interaction holding the containment for the new {@link Message}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether a {@link Message} gets created or
     *            edited, not <code>null</code>
     * @param editMessage
     *            the {@link Message} to edit, not <code>null</code>
     */
    public MessageDialog(final Interaction inter, final ModelUtilities.DialogOps op, final Message editMessage) {
        super();
        this.interaction = inter;
        this.op = op;
        this.editMessage = editMessage;
        this.setWindowProperties("Edit Message");
        this.initialize();
        this.setEditProperties();
        this.setVisible(true);
    }

    /**
     * Sets the {@link JFrame}'s properties
     * 
     * @param title
     *            the title for the {@link JFrame}, not <code>null</code>
     */
    private void setWindowProperties(final String title) {
        this.setTitle(title);
        this.setSize(new Dimension(400, 390));
        this.setLocation(ModelUtilities.getCenterPosition(this));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * Initializes data and swing components.
     */
    private void initialize() {
        // Get lifelines and messages
        this.messages = this.interaction.getMessages();
        this.lifelines = (Lifeline[]) this.interaction.getLifelines().toArray();
        this.filteredMessages = ModelUtilities.getRelevantMessages(this.messages, this.lifelines[0]);

        // String arrays for the comboboxes
        final String[] strLifelines = ModelUtilities.lifelinesToStrings(this.lifelines);
        final String[] strMessageSort = ModelUtilities.valuesToStrings(MessageSort.values());
        final String[] strMessageKind = ModelUtilities.valuesToStrings(MessageKind.values());
        final String[] strMessages = ModelUtilities.messagesToStrings(this.filteredMessages.toArray());

        this.initializeComponents(strLifelines, strMessageSort, strMessageKind, strMessages);
        this.setLabels();
        this.addComponents();
        this.setLayout();
        this.setActionListeners();
    }

    /**
     * Initializes the swing components.
     * 
     * @param strLifelines
     *            the string array containing the names of all existing {@link Lifeline}s, not
     *            <code>null</code>
     * @param strMessageSort
     *            the string array containing the {@link MessageSort} literals, not
     *            <code>null</code>
     * @param strMessageKind
     *            the string array containing the {@link MessageKind} literals, not
     *            <code>null</code>
     * @param strMessages
     *            the string array containing the relvant {@link Message} names, null if there are
     *            no messages
     */
    private void initializeComponents(final String[] strLifelines, final String[] strMessageSort,
            final String[] strMessageKind, final String[] strMessages) {
        this.lblName = new JLabel("Message Name:");
        this.lblOriginLifeline = new JLabel("Origin Lifeline:");
        this.lblDestinationLifeline = new JLabel("Destination Lifeline:");
        this.lblMessageSort = new JLabel("Message Sort:");
        this.lblMessageKind = new JLabel("Message Kind:");
        this.lblInsert = new JLabel("Insert After/Before:");
        this.lblAfterBefore = new JLabel("");
        this.lblSignature = new JLabel("Without Signature:");
        this.lblParameters = new JLabel("Parameters:");
        this.lblReturns = new JLabel("Return:");

        this.txtName = new JTextField();
        this.txtParameters = new JTextField();
        this.txtReturns = new JTextField();

        this.cmbOriginLifeline = new JComboBox<String>(strLifelines);
        this.cmbDestinationLifeline = new JComboBox<String>(strLifelines);
        this.cmbMessageSort = new JComboBox<String>(strMessageSort);
        this.cmbMessageKind = new JComboBox<String>(strMessageKind);
        this.cmbInsert = new JComboBox<String>(strMessages);
        this.cmbAfterBefore = new JComboBox<String>(new String[] { "After", "Before" });

        this.chSignature = new JCheckBox();

        this.btnAccept = new JButton("Ok");
        this.btnCancel = new JButton("Cancel");

        this.contentPane = (JPanel) this.getContentPane();
    }

    /**
     * Adds the components to the {@link JFrame}'s content pane.
     */
    private void addComponents() {
        this.contentPane.add(this.lblName);
        this.contentPane.add(this.txtName);
        this.contentPane.add(this.lblOriginLifeline);
        this.contentPane.add(this.cmbOriginLifeline);
        this.contentPane.add(this.lblDestinationLifeline);
        this.contentPane.add(this.cmbDestinationLifeline);
        this.contentPane.add(this.lblMessageSort);
        this.contentPane.add(this.cmbMessageSort);
        this.contentPane.add(this.lblMessageKind);
        this.contentPane.add(this.cmbMessageKind);
        this.contentPane.add(this.lblInsert);
        this.contentPane.add(this.cmbInsert);
        this.contentPane.add(this.lblAfterBefore);
        this.contentPane.add(this.cmbAfterBefore);
        this.contentPane.add(this.lblSignature);
        this.contentPane.add(this.chSignature);
        this.contentPane.add(this.lblParameters);
        this.contentPane.add(this.txtParameters);
        this.contentPane.add(this.lblReturns);
        this.contentPane.add(this.txtReturns);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    /**
     * Sets the labels for the components.
     */
    private void setLabels() {
        this.lblName.setLabelFor(this.txtName);
        this.lblOriginLifeline.setLabelFor(this.cmbOriginLifeline);
        this.lblOriginLifeline.setLabelFor(this.cmbDestinationLifeline);
        this.lblMessageSort.setLabelFor(this.cmbMessageSort);
        this.lblMessageKind.setLabelFor(this.cmbMessageKind);
        this.lblInsert.setLabelFor(this.cmbInsert);
        this.lblAfterBefore.setLabelFor(this.cmbAfterBefore);
        this.lblSignature.setLabelFor(this.chSignature);
        this.lblParameters.setLabelFor(this.txtParameters);
        this.lblReturns.setLabelFor(this.txtReturns);
    }

    /**
     * Initializes and sets the layout.
     */
    private void setLayout() {
        final SpringLayout layout = new SpringLayout();
        this.contentPane.setLayout(layout);
        SpringUtilities.makeCompactGrid(this.contentPane, 11, 2, // rows, cols
                6, 3, // initX, initY
                6, 3); // xPad, yPad
    }

    /**
     * Sets the action listeners for the components.
     */
    private void setActionListeners() {
        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(MessageDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        final Message selMsg = MessageDialog.this.filteredMessages
                                .get(MessageDialog.this.cmbInsert.getSelectedIndex());
                        final MessageOccurrenceSpecification sendE = (MessageOccurrenceSpecification) selMsg
                                .getSendEvent();
                        final MessageOccurrenceSpecification recvE = (MessageOccurrenceSpecification) selMsg
                                .getReceiveEvent();
                        MessageDialog.this.insert = sendE
                                .getCovered() == MessageDialog.this.lifelines[MessageDialog.this.cmbOriginLifeline
                                        .getSelectedIndex()] ? sendE : recvE;
                        final ModelUtilities.InsertPoint insertPoint = ((String) MessageDialog.this.cmbAfterBefore
                                .getSelectedItem()).equals("After") ? ModelUtilities.InsertPoint.AFTER
                                        : ModelUtilities.InsertPoint.BEFORE;

                        if (MessageDialog.this.op == ModelUtilities.DialogOps.EDIT) {
                            // TODO: Edit function call
                        } else {
                            // Create function call
                            ModelOperations.createMessage(MessageDialog.this.interaction,
                                    MessageDialog.this.txtName.getText(),
                                    MessageDialog.this.lifelines[MessageDialog.this.cmbOriginLifeline
                                            .getSelectedIndex()],
                                    MessageDialog.this.lifelines[MessageDialog.this.cmbDestinationLifeline
                                            .getSelectedIndex()],
                                    MessageSort.values()[MessageDialog.this.cmbMessageSort.getSelectedIndex()],
                                    MessageKind.values()[MessageDialog.this.cmbMessageKind.getSelectedIndex()],
                                    MessageDialog.this.insert, insertPoint,
                                    ModelUtilities.getParameters(MessageDialog.this.txtParameters.getText()),
                                    ModelUtilities.getReturn(MessageDialog.this.txtReturns.getText()));
                        }
                    }
                });
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }

        });
        this.btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
        this.cmbOriginLifeline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MessageDialog.this.setInsertBox(
                        MessageDialog.this.lifelines[MessageDialog.this.cmbOriginLifeline.getSelectedIndex()]);
            }
        });
        this.chSignature.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (MessageDialog.this.txtParameters.isEnabled() && MessageDialog.this.txtReturns.isEnabled()) {
                    MessageDialog.this.txtParameters.setText("");
                    MessageDialog.this.txtReturns.setText("");
                    MessageDialog.this.txtParameters.setEnabled(false);
                    MessageDialog.this.txtReturns.setEnabled(false);
                    MessageDialog.this.lblParameters.setEnabled(false);
                    MessageDialog.this.lblReturns.setEnabled(false);
                } else {
                    MessageDialog.this.txtParameters.setEnabled(true);
                    MessageDialog.this.txtReturns.setEnabled(true);
                    MessageDialog.this.lblParameters.setEnabled(true);
                    MessageDialog.this.lblReturns.setEnabled(true);
                }
            }
        });
        this.cmbInsert.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final Lifeline l = MessageDialog.this.lifelines[MessageDialog.this.cmbOriginLifeline
                        .getSelectedIndex()];
                final Message m = MessageDialog.this.filteredMessages
                        .get(MessageDialog.this.cmbInsert.getSelectedIndex());
                final MessageOccurrenceSpecification mos = ((MessageOccurrenceSpecification) m.getSendEvent())
                        .getCovered() == l ? (MessageOccurrenceSpecification) m.getSendEvent()
                                : (MessageOccurrenceSpecification) m.getReceiveEvent();
                final ActionExecutionSpecification soe = ModelUtilities.isExecutionStartOrEnd(l, mos);
                // Check if the message occurrence specification of the selected message on the
                // originating lifeline is a start or finish point on an action execution, if it is
                // disable the after/before combobox
                if (soe != null) {
                    MessageDialog.this.lblAfterBefore.setEnabled(true);
                    MessageDialog.this.cmbAfterBefore.setEnabled(true);
                } else {
                    MessageDialog.this.lblAfterBefore.setEnabled(false);
                    MessageDialog.this.cmbAfterBefore.setEnabled(false);
                }

            }

        });
    }

    /**
     * Sets the properties of the {@link Message} to edit.
     */
    private void setEditProperties() {
        this.txtName.setText(this.editMessage.getName());
        this.cmbOriginLifeline.setEnabled(false);
        this.cmbDestinationLifeline.setEnabled(false);
        this.lblOriginLifeline.setEnabled(false);
        this.lblDestinationLifeline.setEnabled(false);
        this.cmbMessageSort.setSelectedItem(this.editMessage.getMessageSort().getName());
        this.cmbMessageKind.setSelectedItem(this.editMessage.getMessageKind().getName());
        this.setInsertBox(((MessageOccurrenceSpecification) this.editMessage.getSendEvent()).getCovered());
    }

    /**
     * Updates the insert combobox with the relevant {@link Message}s for the selected origin
     * {@link Lifeline}.
     * 
     * @param l
     *            the currently selected origin {@link Lifeline}, not <code>null</code>
     */
    private void setInsertBox(final Lifeline l) {
        this.filteredMessages = ModelUtilities.getRelevantMessages(this.messages, l);
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(
                ModelUtilities.messagesToStrings(this.filteredMessages.toArray()));
        this.cmbInsert.setModel(model);
    }

}