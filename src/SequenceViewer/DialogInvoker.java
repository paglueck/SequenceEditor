package SequenceViewer;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.action.AbstractExternalJavaAction;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.StateInvariant;

/**
 * Class representing the java action call that can be used inside of the sirius editor.
 * <p>
 * Tools inside the sirius editor definition define a parameters for the type ("create", "edit", "del") and 
 * element type ("LL" ( {@link Lifeline} ), "Msg" ( {@link Message} ), "CF" ( {@link CombinedFragment} ), "SI" ( {@link StateInvariant} ), "IO" ( {@link InteractionOperand} )) 
 * to call the corresponding dialog/method
 */
public class DialogInvoker extends AbstractExternalJavaAction implements IExternalJavaAction {

    @Override
    public boolean canExecute(final Collection<? extends EObject> args) {
        return true;
    }

    @Override
    public void execute(final Collection<? extends EObject> args, final Map<String, Object> arg1) {
        
        final String elementType = (String) arg1.get("type");
        final String operationType = (String) arg1.get("op");
        final Interaction t = (Interaction) arg1.get("interaction");
        final Lifeline l = (Lifeline) arg1.get("lifeline");
        final StateInvariant i = (StateInvariant) arg1.get("stateinvariant");
        final CombinedFragment cf = (CombinedFragment) arg1.get("combinedfragment");
        final Message msg = (Message) arg1.get("message");
        
        // Lifeline dialog branch
        if (elementType.equals("'LL'")) {
            LifelineDialog cld;
            if (operationType.equals("'edit'")) {
                cld = new LifelineDialog(l.getInteraction(), ModelUtilities.DialogOps.EDIT, l);
            } else if (operationType.contentEquals("'del'")) {
                ModelOperations.deleteLifeline(l.getInteraction(), l);
            } else {
                cld = new LifelineDialog(t, ModelUtilities.DialogOps.CREATE);
            }
        }

        // Message dialog branch        
        if (elementType.equals("'Msg'")) {
            MessageDialog cmd;
            if (operationType.equals("'edit'")) {
                cmd = new MessageDialog(msg.getInteraction(), ModelUtilities.DialogOps.EDIT, msg);
            } else if (operationType.equals("'del'")) {
                ModelOperations.deleteMessage(msg);
            } else if (operationType.equals("'create'")) {
                cmd = new MessageDialog(t, ModelUtilities.DialogOps.CREATE);
            }
        } 
        
        // State invariant dialog branch
        if (elementType.equals("'SI'")) {
            StateInvariantDialog cid;
            if (operationType.equals("'edit'")) {
                cid = new StateInvariantDialog(i.getEnclosingInteraction(), ModelUtilities.DialogOps.EDIT, i);
            } else if (operationType.equals("'del'")) {
                ModelOperations.deleteStateInvariant(i.getEnclosingInteraction(), i);
            } else {
                cid = new StateInvariantDialog(t, ModelUtilities.DialogOps.CREATE);
            }
        } 
        
        // Combined fragment dialog branch
        if (elementType.equals("'CF'")) {
            CombinedFragmentDialog ccd;
            if (operationType.equals("'edit'")) {
                ccd = new CombinedFragmentDialog(cf.getEnclosingInteraction(), ModelUtilities.DialogOps.EDIT, cf);
            } else if (operationType.equals("'del'")) {
                // TODO: delete functionality
            } else {
                ccd = new CombinedFragmentDialog(t, ModelUtilities.DialogOps.CREATE);
            }            
        }
        
        // Interaction operand dialog branch
        if (elementType.equals("'IO'")) {
            OperandDialog od;
            if (operationType.equals("'edit'")) {
                od = new OperandDialog(cf.getEnclosingInteraction(), ModelUtilities.DialogOps.EDIT, cf);
            } else if (operationType.equals("'del'")) {
                // TODO: delete functionality
            } else {
                od = new OperandDialog(cf, ModelUtilities.DialogOps.CREATE);
            }
        }

    }

}
