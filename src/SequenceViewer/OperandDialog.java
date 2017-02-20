package SequenceViewer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;

import SequenceViewer.ModelUtilities.DialogOps;

/**
 * Class for providing a dialog window for creating and editing {@link InteractionOperand}s.
 */
public class OperandDialog extends JFrame {

    private static final long serialVersionUID = 1L;

    // Variables declaration
    private JLabel lblConstraint;

    private JTextField txtConstraint;
    private JButton btnAccept;
    private JButton btnCancel;
    private JPanel contentPane;

    private final Interaction interaction;
    private final CombinedFragment comFragment;
    private final ModelUtilities.DialogOps op;
    // End of variables declaration

    /**
     * Constructor for a dialog for editing an existing {@link InteractionOperand}.
     * 
     * @param inter
     *            the interaction providing the context for the {@link InteractionOperand}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link InteractionOperand}
     *            will be created or edited, not <code>null</code>
     * @param cf
     *            the {@link CombinedFragment} holding the {@link InteractionOperand} to be edited,
     *            not <code>null</code>
     */
    public OperandDialog(final Interaction inter, final ModelUtilities.DialogOps operation, final CombinedFragment cf) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.comFragment = cf;
        // TODO: actual edit functionality
        this.setWindowProperties("Edit InteractionOperands");
        this.initialize();
        this.setEditProperties();
        this.setVisible(true);
    }

    /**
     * Constructor for a dialog for creating a new {@link InteractionOperand}.
     * 
     * @param cf
     *            the {@link CombinedFragment} holding the {@link InteractionOperand} to be edited,
     *            not <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link InteractionOperand}
     *            will be created or edited, not <code>null</code>
     */
    public OperandDialog(final CombinedFragment cf, final ModelUtilities.DialogOps operation) {
        super();
        this.interaction = cf.getEnclosingInteraction();
        this.comFragment = cf;
        this.op = operation;
        this.setWindowProperties("Add New InteractionOperand");
        this.initialize();
        this.setVisible(true);
    }

    private void initialize() {

        // Initialize the swing components
        this.initializeComponents();

        // Set the labels for the components
        this.setLabels();

        // Add the components to the content pane
        this.addComponents();

        // Layout
        this.setLayout();

        // Action listeners for the buttons
        this.setActionListeners();
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
        this.setSize(new Dimension(400, 90));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * Initializes the swing components for the dialog.
     */
    private void initializeComponents() {
        this.lblConstraint = new JLabel("Constraint:");
        this.txtConstraint = new JTextField();
        this.btnAccept = new JButton("Ok");
        this.btnCancel = new JButton("Cancel");
        this.contentPane = (JPanel) this.getContentPane();
    }

    /**
     * Adds the swing components to the {@link JFrame}'s content pane.
     */
    private void addComponents() {
        this.contentPane.add(this.lblConstraint);
        this.contentPane.add(this.txtConstraint);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    /**
     * Sets the labels for the components.
     */
    private void setLabels() {
        this.lblConstraint.setLabelFor(this.txtConstraint);
    }

    /**
     * Initializes and sets the layout.
     */
    private void setLayout() {
        final SpringLayout layout = new SpringLayout();
        this.contentPane.setLayout(layout);
        SpringUtilities.makeCompactGrid(this.contentPane, 2, 2, // rows, cols
                6, 3, // initX, initY
                6, 3); // xPad, yPad
    }

    /**
     * Sets the action listeners for the buttons.
     */
    private void setActionListeners() {
        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(OperandDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        if (OperandDialog.this.op == DialogOps.EDIT) {
                            // TODO: edit functionality   
                        } else {
                            ModelOperations.createInteractionOperand(OperandDialog.this.comFragment,
                                    OperandDialog.this.txtConstraint.getText());   
                        }
                    }
                });
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }

        });
        this.btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    }

    /**
     * Sets the properties for editing an {@link InteractionOperand}.
     */
    private void setEditProperties() {
        // TODO: edit feature
    }
}