package SequenceViewer;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.business.api.action.AbstractExternalJavaAction;
import javax.swing.*;

import org.eclipse.uml2.*;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

public class ExternalAction extends AbstractExternalJavaAction {

	@Override
	public boolean canExecute(Collection<? extends EObject> arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void execute(Collection<? extends EObject> arg0, Map<String, Object> arg1) {
		// TODO Auto-generated method stub
		String s = (String) arg1.get("type");
		Interaction t = (Interaction) arg1.get("interaction");
		System.out.println("Class: " + t.eClass().getName());
		System.out.println("Name: " + t.getName());
		System.out.println(s);
		Lifeline l1 = t.getLifeline("AddMoviesLifeline");
		Lifeline l2 = t.getLifeline("MainFrameLifeline");
		
		if (s.equals("'newLL'")) {
			String testLifeline = "TestLifeline";
			Lifeline l = (Lifeline) t.createLifeline(testLifeline);
			l.setRepresents(t.getOwnedAttributes().get(0));
		} else if (s.equals("'newSI'")) {
			String testInvariant = "TestInvariant";
			StateInvariant si = (StateInvariant) t.createFragment(testInvariant, UMLPackage.eINSTANCE.getStateInvariant());
			
			Constraint con = (Constraint) si.createInvariant("TestInvariantConstraint", UMLPackage.eINSTANCE.getConstraint());
			JOptionPane.showMessageDialog(null, "Eggs are not supposed to be green.");
					
		} else if (s.equals("'newCF'")) {
			String testCF = "TestCF";
			CombinedFragment cf = (CombinedFragment) t.createFragment(testCF, UMLPackage.eINSTANCE.getCombinedFragment());
			cf.setInteractionOperator(InteractionOperatorKind.LOOP_LITERAL);
		} else {
			String testMessageName = "TestMessage";
			String cc = "_Call";
			
			Message message = t.createMessage(testMessageName);
			
			MessageOccurrenceSpecification sendEventOccur = (MessageOccurrenceSpecification) t.createFragment(testMessageName+cc+"Send",UMLPackage.eINSTANCE.getMessageOccurrenceSpecification());
			sendEventOccur.getCovereds().add(l1);
			sendEventOccur.setMessage(message);
					
			MessageOccurrenceSpecification recvEventOccur = (MessageOccurrenceSpecification) t.createFragment(testMessageName+cc+"Recv",UMLPackage.eINSTANCE.getMessageOccurrenceSpecification());
			recvEventOccur.getCovereds().add(l2);
			recvEventOccur.setMessage(message);

			message.setSendEvent(sendEventOccur);
			message.setReceiveEvent(recvEventOccur);

			org.eclipse.uml2.uml.Class p = (org.eclipse.uml2.uml.Class) t.getModel().getPackagedElement(l1.getRepresents().getType().getName());
			Operation o = p.createOwnedOperation(testMessageName, null, null);
			
			message.setSignature(o);
			
			System.out.println("Created Testmessage!");
		}		
		
	}
	
		
}
