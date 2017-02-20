package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;

import SequenceViewer.ModelUtilities.DialogOps;

/**
 * Class for providing a dialog window for creating or editing a {@link CombinedFragment}.
 */
public class CombinedFragmentDialog extends JFrame {

    private static final long serialVersionUID = 1L;
    
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
    private EList<Lifeline> lifelineList;

    private final Interaction interaction;
    private CombinedFragment editFragment;
    private final ModelUtilities.DialogOps op;
    // End of variables declaration

    /**
     * Constructor for a dialog for creating a new {@link CombinedFragment}.
     * 
     * @param inter
     *            the interaction holding the containment for the new {@link CombinedFragment}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether a {@link CombinedFragment} gets
     *            created or edited, not <code>null</code>
     */
    public CombinedFragmentDialog(final Interaction inter, final ModelUtilities.DialogOps operation) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.setWindowProperties("Create New Combined Fragment");
        this.initialize();
        this.setVisible(true);
    }

    /**
     * Constructor for creating a dialog for editing an existing {@link CombinedFragment}.
     * 
     * @param inter
     *            the interaction which holds the {@link CombinedFragment}, not <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether a {@link CombinedFragment} gets
     *            created or edited, not <code>null</code>
     * @param editFragment
     *            the {@link CombinedFragment} to edit, not <code>null</code>
     */
    public CombinedFragmentDialog(final Interaction inter, final ModelUtilities.DialogOps operation,
            final CombinedFragment editFragment) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.editFragment = editFragment;
        this.setWindowProperties("Edit Combined Fragment");
        this.initialize();
        this.setEditProperties();
        this.setVisible(true);
    }

    /**
     * Initializes the necessary data and swing components
     */
    private void initialize() {
        // Get lifelines
        this.lifelineList = this.interaction.getLifelines();
        this.lifelines = (Lifeline[]) this.lifelineList.toArray();

        // Get messages
        final EList<Message> messages = this.interaction.getMessages();
        final List<Message> filteredMessages = ModelUtilities.getRelevantMessages(messages, this.lifelines[0]);

        // Get strings for the comboboxes
        final String[] strLifelines = ModelUtilities.lifelinesToStrings(this.lifelines);
        final String[] strTypes = new String[] { "Alt", "Opt", "Par", "Critical", "Loop" };
        final String[] strMessages = ModelUtilities.messagesToStrings(filteredMessages.toArray());

        // Initialize the swing components
        this.initializeComponents(strLifelines, strTypes, strMessages);

        // JList properties
        this.lstLifelines.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.lstLifelines.setLayoutOrientation(JList.VERTICAL);
        this.lstLifelines.setVisibleRowCount(2);

        // Scrollpane properties
        this.scrllList = new JScrollPane(this.lstLifelines);
        this.scrllList.setPreferredSize(new Dimension(250, 100));

        this.setLabels();
        this.addComponents();
        this.setLayout();
        this.setActionListeners();
    }

    /**
     * Initializes the swing components.
     * 
     * @param strLifelines
     *            the {@link Lifeline} names as string array, not <code>null</code>
     * @param strTypes
     *            the {@link InteractionOperatorKind} literals as string array, not
     *            <code>null</code>
     * @param strMessages
     *            the {@link Message} names as string array, <code>null</code>
     */
    private void initializeComponents(final String[] strLifelines, final String[] strTypes,
            final String[] strMessages) {
        this.lblName = new JLabel("CF Name:");
        this.lblType = new JLabel("Type:");
        this.lblConstraint = new JLabel("Constraint:");
        this.lblInsert = new JLabel("Insert After/Before: ");
        this.lblLifelines = new JLabel("Lifeline Coverage:");

        this.txtName = new JTextField(1);
        this.txtConstraint = new JTextField(1);

        this.cmbType = new JComboBox<String>(strTypes);
        this.cmbInsert = new JComboBox<String>(strMessages);

        this.lstLifelines = new JList<String>(strLifelines);

        this.btnAccept = new JButton("Ok");
        this.btnCancel = new JButton("Cancel");

        this.contentPane = (JPanel) this.getContentPane();
    }

    /**
     * Adds the swing components to the {@link JFrame}'s content pane.
     */
    private void addComponents() {
        this.contentPane.add(this.lblName);
        this.contentPane.add(this.txtName);
        this.contentPane.add(this.lblType);
        this.contentPane.add(this.cmbType);
        this.contentPane.add(this.lblConstraint);
        this.contentPane.add(this.txtConstraint);
        this.contentPane.add(this.lblInsert);
        this.contentPane.add(this.cmbInsert);
        this.contentPane.add(this.lblLifelines);
        this.contentPane.add(this.scrllList);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    /**
     * Sets the labels for the components.
     */
    private void setLabels() {
        this.lblName.setLabelFor(this.txtName);
        this.lblType.setLabelFor(this.cmbType);
        this.lblConstraint.setLabelFor(this.txtConstraint);
        this.lblInsert.setLabelFor(this.cmbInsert);
        this.lblLifelines.setLabelFor(this.scrllList);
    }

    /**
     * Initializes and sets the layout.
     */
    private void setLayout() {
        final SpringLayout layout = new SpringLayout();
        this.contentPane.setLayout(layout);
        SpringUtilities.makeCompactGrid(this.contentPane, 6, 2, // rows, cols
                6, 3, // initX, initY
                6, 3); // xPad, yPad
    }

    /**
     * Sets the {@link JFrame}'s properties.
     *
     * @param title
     *            the title for the {@link JFrame}, not <code>null</code>
     */
    private void setWindowProperties(final String title) {
        this.setTitle(title);
        this.setSize(new Dimension(400, 300));
        this.setLocation(ModelUtilities.getCenterPosition(this));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * Sets the properties of the {@link CombinedFragment} that gets edited.
     */
    private void setEditProperties() {
        // Set name
        this.txtName.setText(this.editFragment.getName());

        // Set interaction operator kind
        this.cmbType.setSelectedItem(this.editFragment.getInteractionOperator().getName());

        // Get lifelines covered by the combined fragment
        final EList<Lifeline> templist = this.editFragment.getCovereds();

        // Get the selected indices for the lifelines and set the selection
        final int[] indices = new int[templist.size()];
        int i = 0;
        for (final Lifeline l : templist) {
            indices[i] = this.lifelineList.indexOf(l);
            i++;
        }
        this.lstLifelines.setSelectedIndices(indices);
    }

    /**
     * Sets the action listeners for the buttons.
     */
    private void setActionListeners() {
        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(CombinedFragmentDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        if (CombinedFragmentDialog.this.op == ModelUtilities.DialogOps.EDIT) {
                            // TODO: Edit function call
                        } else {
                            // Create function call
                            ModelOperations.createCombinedFragment(CombinedFragmentDialog.this.interaction,
                                    CombinedFragmentDialog.this.txtName.getText(),
                                    ModelUtilities.getInteractionOperatorKind(
                                            (String) CombinedFragmentDialog.this.cmbType.getSelectedItem()),
                                    ModelUtilities.getSelectedLifelines(CombinedFragmentDialog.this.lifelines,
                                            CombinedFragmentDialog.this.lstLifelines.getSelectedIndices()),
                                    CombinedFragmentDialog.this.txtConstraint.getText());
                        }

                    }
                });
                // Dispose window after creating/editing
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }

        });

        this.btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    }

}