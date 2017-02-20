package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.PackageableElement;

import SequenceViewer.ModelUtilities.DialogOps;

/**
 * Class providing a dialog window for creating and editing {@link Lifeline}s.
 */
public class LifelineDialog extends JFrame {

    private static final long serialVersionUID = 1L;

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

    private final Interaction interaction;
    private Lifeline editLifeline;
    private final ModelUtilities.DialogOps op;
    // End of variables declaration

    /**
     * Constructor for creating a dialog for editing an existing {@link Lifeline}.
     * 
     * @param inter
     *            the interaction holding the containment with the {@link Lifeline}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link Lifeline} will be
     *            created or edited, not <code>null</code>
     * @param editLifeline
     *            the {@link Lifeline} to edit, not <code>null</code>
     */
    public LifelineDialog(final Interaction inter, final ModelUtilities.DialogOps operation,
            final Lifeline editLifeline) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.editLifeline = editLifeline;
        this.setWindowProperties();
        this.initialize();
        this.setEditProperties();
        this.setVisible(true);
    }

    /**
     * Constructor for creating a dialog for editing an existing {@link Lifeline}.
     * 
     * @param inter
     *            the interaction holding the containment with the {@link Lifeline}, not
     *            <code>null</code>
     * @param operation
     *            the {@link DialogOps} enum representing wether if a {@link Lifeline} will be
     *            created or edited, not <code>null</code>
     */
    public LifelineDialog(final Interaction inter, final ModelUtilities.DialogOps operation) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.setWindowProperties();
        this.initialize();
        this.setVisible(true);
    }

    /**
     * Sets the {@link JFrame}'s properties.
     */
    private void setWindowProperties() {
        if (this.op == ModelUtilities.DialogOps.EDIT) {
            this.setTitle("Edit StateInvariant");
        } else {
            this.setTitle("Create New StateInvariant");
        }
        this.setLocation(new Point(500, 500));
        this.setSize(new Dimension(400, 150));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * Initializes the data and swing components.
     */
    private void initialize() {

        // Fetch all possible classes the lifeline could represent
        final EList<PackageableElement> pes = this.interaction.getModel().getPackagedElements();
        final List<PackageableElement> ps = new ArrayList<PackageableElement>();
        for (final PackageableElement el : pes) {
            if ((el.eClass().getName().equals("Class"))) {
                ps.add(el);
            }
        }

        // Create string array with class names for the combobox
        final String[] strClasses = new String[ps.size()];
        for (int i = 0; i < ps.size(); i++) {
            strClasses[i] = ps.get(i).getName();
        }

        // Get lifelines and a string array with all lifeline names for the combobox
        final EList<Lifeline> lifelines = this.interaction.getLifelines();
        final String[] strLifelines = ModelUtilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());

        if (this.op == ModelUtilities.DialogOps.EDIT) {
            this.initializeComponents(strClasses, strLifelines);
        } else {
            this.initializeComponents(strClasses, ModelUtilities.concat(strLifelines, new String[] { "End" }));
        }

        this.setLabels();
        this.addComponents();
        this.setLayout();
        this.setActionListeners(ps, lifelines);
    }

    /**
     * Initializes the swing components.
     * 
     * @param classes
     *            the string array containing the names of all classes, not <code>null</code>
     * @param lifelines
     *            the string array containing the names of all existing {@link Lifeline}s, not
     *            <code>null</code>
     */
    private void initializeComponents(final String[] classes, final String[] lifelines) {
        this.lblName = new JLabel("Lifeline Instance Name: ");
        this.lblClass = new JLabel("Class: ");
        if (this.op == ModelUtilities.DialogOps.EDIT) {
            this.lblInsert = new JLabel("Insert After: ");
        } else {
            this.lblInsert = new JLabel("Insert Before: ");
        }
        this.txtName = new JTextField(3);
        this.cmbClass = new JComboBox<String>(classes);
        this.cmbInsert = new JComboBox<String>(lifelines);
        this.btnAccept = new JButton("Ok");
        this.btnCancel = new JButton("Cancel");
        this.contentPane = (JPanel) this.getContentPane();
    }

    /**
     * Adds all components to the {@link JFrame}'s content pane.
     */
    private void addComponents() {
        this.contentPane.add(this.lblName);
        this.contentPane.add(this.txtName);
        this.contentPane.add(this.lblClass);
        this.contentPane.add(this.cmbClass);
        this.contentPane.add(this.lblInsert);
        this.contentPane.add(this.cmbInsert);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    /**
     * Set the labels for the components.
     */
    private void setLabels() {
        this.lblName.setLabelFor(this.txtName);
        this.lblClass.setLabelFor(this.cmbClass);
        this.lblInsert.setLabelFor(this.cmbInsert);
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
     * Set the action listeners.
     * 
     * @param ps
     *            the list containing all possible {@link Class}es, not <code>nul</code>
     * @param lifelines
     *            the {@link EList} containing all existing {@link Lifeline}s, null if there are no
     *            lifelines
     */
    private void setActionListeners(final List<PackageableElement> ps, final EList<Lifeline> lifelines) {
        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(LifelineDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        if (LifelineDialog.this.op == ModelUtilities.DialogOps.EDIT) {
                            // Edit function call
                            ModelOperations.editLifeline(LifelineDialog.this.editLifeline, lifelines,
                                    LifelineDialog.this.txtName.getText(),
                                    (org.eclipse.uml2.uml.Class) ps
                                            .get(LifelineDialog.this.cmbClass.getSelectedIndex()),
                                    lifelines.get(LifelineDialog.this.cmbInsert.getSelectedIndex()));
                        } else {
                            // Create function call
                            ModelOperations.createLifeline(LifelineDialog.this.interaction, lifelines,
                                    LifelineDialog.this.txtName.getText(),
                                    (org.eclipse.uml2.uml.Class) ps
                                            .get(LifelineDialog.this.cmbClass.getSelectedIndex()),
                                    LifelineDialog.this.cmbInsert.getSelectedIndex());
                        }
                    }
                });
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }

        });
        this.btnCancel.addActionListener(ModelUtilities.getCancelActionListener());
    }

    /**
     * Set the properties of the lifeline to edit.
     */
    private void setEditProperties() {
        this.txtName.setText(this.editLifeline.getRepresents().getName());
        this.cmbClass.setSelectedItem(this.editLifeline.getRepresents().getType().getName());
        this.cmbInsert.setSelectedItem(this.editLifeline.getName());
    }
}