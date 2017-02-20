package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.StateInvariant;

import SequenceViewer.ModelUtilities.DialogOps;

/**
 * Class providing a dialog window for creating and editing {@link StateInvariant}s.
 */
public class StateInvariantDialog extends JFrame {

    private static final long serialVersionUID = 1L;

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

    private final Interaction interaction;
    private StateInvariant editInvariant;
    private final ModelUtilities.DialogOps op;
    // End of variables declaration

    /**
     * Constructor for a dialog for creating a new {@link StateInvariant}.
     *
     * @param inter
     *            the interaction providing the context for the new {@link StateInvariant}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link StateInvariant} will be
     *            created or edited, not <code>null</code>
     */
    public StateInvariantDialog(final Interaction inter, final ModelUtilities.DialogOps operation) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.setWindowProperties("Create New StateInvariant");
        this.initialize();
        this.setVisible(true);
    }

    /**
     * Constructor for a dialog for editing an existing {@link StateInvariant}.
     *
     * @param inter
     *            the interaction providing the context for the new {@link StateInvariant}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link StateInvariant} will be
     *            created or edited, not <code>null</code>
     * @param editInvariant
     *            the {@link StateInvariant} that gets edited, not <code>null</code>
     */
    public StateInvariantDialog(final Interaction inter, final ModelUtilities.DialogOps operation,
            final StateInvariant editInvariant) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.editInvariant = editInvariant;
        this.setWindowProperties("Edit StateInvariant");
        this.initialize();
        this.setEditProperties();
        this.setVisible(true);
    }

    /**
     * Sets the {@link JFrame}'s properties.
     *
     * @param title
     *            the title for the {@link JFrame}, not <code>null</code>
     */
    private void setWindowProperties(final String title) {
        this.setTitle(title);
        this.setLocation(ModelUtilities.getCenterPosition(this));
        this.setSize(new Dimension(400, 150));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * Initializes the elements and data for the dialog frame.
     */
    private void initialize() {
        // Get all lifelines and their names as string array for the combobox model
        final EList<Lifeline> lifelines = this.interaction.getLifelines();
        final String[] strLifelines = ModelUtilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());

        // Initialize the swing objects for the dialog
        this.initializeComponents(strLifelines);

        // Set the labels for the corresponding elements
        this.setLabels();

        // Add the components to the contentpane
        this.addComponents();

        // Layout
        this.setLayout();

        // Action listeners for the buttons
        this.setActionListeners(lifelines);
    }

    /**
     * Initializes the swing components for the dialog.
     *
     * @param strLifelines
     *            the names of all {@link Lifeline}s as string array, null if there are no lifelines
     */
    private void initializeComponents(final String[] strLifelines) {
        this.lblName = new JLabel("StateInvariant Name: ");
        this.lblLifeline = new JLabel("For Lifeline:");
        this.lblConstraint = new JLabel("Constraint: ");
        this.txtName = new JTextField(3);
        this.txtConstraint = new JTextField(3);
        this.cmbLifeline = new JComboBox<String>(strLifelines);
        this.btnAccept = new JButton("Ok");
        this.btnCancel = new JButton("Cancel");
        this.contentPane = (JPanel) this.getContentPane();
    }

    /**
     * Sets the labels for the corresponding components.
     */
    private void setLabels() {
        this.lblName.setLabelFor(this.txtName);
        this.lblLifeline.setLabelFor(this.cmbLifeline);
        this.lblConstraint.setLabelFor(this.txtConstraint);
    }

    /**
     * Adds all components to the {@link JFrame}'s content pane.
     */
    private void addComponents() {
        this.contentPane.add(this.lblName);
        this.contentPane.add(this.txtName);
        this.contentPane.add(this.lblLifeline);
        this.contentPane.add(this.cmbLifeline);
        this.contentPane.add(this.lblConstraint);
        this.contentPane.add(this.txtConstraint);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    /**
     * Initialize and set the layout.
     */
    private void setLayout() {
        final SpringLayout layout = new SpringLayout();
        this.contentPane.setLayout(layout);
        SpringUtilities.makeCompactGrid(this.contentPane, 4, 2, // rows, cols
                6, 3, // initX, initY
                6, 3); // xPad, yPad
    }

    /**
     * Set the action listeners for the buttons.
     * 
     * @param lifelines
     *            the {@link Lifeline}s on which the {@link StateInvariant} can be placed, not
     *            <code>null</code>
     */
    private void setActionListeners(final EList<Lifeline> lifelines) {
        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(StateInvariantDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        if (StateInvariantDialog.this.op == ModelUtilities.DialogOps.EDIT) {
                            ModelOperations.editStateInvariant(StateInvariantDialog.this.txtName.getText(),
                                    lifelines.get(StateInvariantDialog.this.cmbLifeline.getSelectedIndex()),
                                    StateInvariantDialog.this.txtConstraint.getText(),
                                    StateInvariantDialog.this.editInvariant);
                        } else {
                            ModelOperations.createStateInvariant(StateInvariantDialog.this.interaction,
                                    StateInvariantDialog.this.txtName.getText(),
                                    lifelines.get(StateInvariantDialog.this.cmbLifeline.getSelectedIndex()),
                                    StateInvariantDialog.this.txtConstraint.getText());
                        }

                    }
                });
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }

        });
        this.btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    }

    /**
     * Sets the properties of the {@link StateInvariant} that gets edited.
     */
    private void setEditProperties() {
        this.txtName.setText(this.editInvariant.getName());
        this.cmbLifeline.setSelectedItem(this.editInvariant.getCovereds().get(0).getName());
        final LiteralString sp = (LiteralString) this.editInvariant.getInvariant().getSpecification();
        this.txtConstraint.setText(sp.getValue());
    }
}