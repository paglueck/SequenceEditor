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
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.PackageableElement;

public class LifelineDialog extends JFrame {

    /**
     *
     */
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

    public LifelineDialog(final Interaction inter, final ModelUtilities.DialogOps operation,
            final Lifeline editLifeline) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.editLifeline = editLifeline;
        this.setWindowProperties();
        this.create();
        this.setVisible(true);
    }

    public LifelineDialog(final Interaction inter, final ModelUtilities.DialogOps operation) {
        super();
        this.interaction = inter;
        this.op = operation;
        this.setWindowProperties();
        this.create();
        this.setVisible(true);
    }

    private void create() {

        final EList<PackageableElement> pes = this.interaction.getModel().getPackagedElements();
        final List<PackageableElement> ps = new ArrayList<PackageableElement>();
        for (final PackageableElement el : pes) {
            if ((el.eClass().getName().equals("Class"))) {
                ps.add(el);
            }
        }

        final String[] strClasses = new String[ps.size()];
        for (int i = 0; i < ps.size(); i++) {
            strClasses[i] = ps.get(i).getName();
        }

        final EList<Lifeline> lifelines = this.interaction.getLifelines();
        final String[] strLifelines = ModelUtilities.lifelinesToStrings((Lifeline[]) lifelines.toArray());

        if (this.op == ModelUtilities.DialogOps.EDIT) {
            this.initializeComponents(strClasses, strLifelines);
        } else {
            this.initializeComponents(strClasses, ModelUtilities.concat(strLifelines, new String[] { "End" }));
        }
        this.setLabels();
        this.addComponents();
        if (this.op == ModelUtilities.DialogOps.EDIT) {
            this.setEditProperties();
        }
        final SpringLayout layout = new SpringLayout();
        this.contentPane.setLayout(layout);

        SpringUtilities.makeCompactGrid(this.contentPane, 4, 2, // rows, cols
                6, 3, // initX, initY
                6, 3); // xPad, yPad

        this.btnAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final TransactionalEditingDomain domain = TransactionUtil
                        .getEditingDomain(LifelineDialog.this.interaction);
                domain.getCommandStack().execute(new RecordingCommand(domain) {

                    @Override
                    protected void doExecute() {
                        if (LifelineDialog.this.op == ModelUtilities.DialogOps.EDIT) {
                            ModelOperations.editLifeline(LifelineDialog.this.interaction,
                                    LifelineDialog.this.editLifeline, lifelines, LifelineDialog.this.txtName.getText(),
                                    (org.eclipse.uml2.uml.Class) ps
                                            .get(LifelineDialog.this.cmbClass.getSelectedIndex()),
                                    lifelines.get(LifelineDialog.this.cmbInsert.getSelectedIndex()));
                        } else {
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

    public void setWindowProperties() {
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

    public void initializeComponents(final String[] classes, final String[] lifelines) {
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

    public void addComponents() {
        this.contentPane.add(this.lblName);
        this.contentPane.add(this.txtName);
        this.contentPane.add(this.lblClass);
        this.contentPane.add(this.cmbClass);
        this.contentPane.add(this.lblInsert);
        this.contentPane.add(this.cmbInsert);
        this.contentPane.add(this.btnAccept);
        this.contentPane.add(this.btnCancel);
    }

    public void setLabels() {
        this.lblName.setLabelFor(this.txtName);
        this.lblClass.setLabelFor(this.cmbClass);
        this.lblInsert.setLabelFor(this.cmbInsert);
    }

    public void setEditProperties() {
        this.txtName.setText(this.editLifeline.getRepresents().getName());
        this.cmbClass.setSelectedItem(this.editLifeline.getRepresents().getType().getName());
        this.cmbInsert.setSelectedItem(this.editLifeline.getName());
    }
}