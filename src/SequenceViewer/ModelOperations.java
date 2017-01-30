package SequenceViewer;

import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.UMLPackage;

public class ModelOperations {

	public static void createLifeline(Interaction interaction, String name, org.eclipse.uml2.uml.Class selClass, String inserAfter) {
		Property p = interaction.createOwnedAttribute(name, selClass, UMLPackage.eINSTANCE.getProperty());
		Lifeline l = (Lifeline) interaction.createLifeline(name+"Lifeline");
		l.setRepresents(p);
	}
	
	public static void createMessage(Interaction interacion, String name, Lifeline origLifeline, Lifeline destLifeline, MessageSort sort, MessageKind kind) {
		// Create new message in interaction containment
		Message message = interacion.createMessage(name);
		
		// Set messageSort property
		message.setMessageSort(sort);
		
		// Create send event for the new message (create new MOS in interaction containment, set covered property to origin lifeline, set message property)
		MessageOccurrenceSpecification sendEventOccur = (MessageOccurrenceSpecification) interacion.createFragment(name+"Send",UMLPackage.eINSTANCE.getMessageOccurrenceSpecification());
		sendEventOccur.getCovereds().add(origLifeline);
		sendEventOccur.setMessage(message);
		
		// Create receive event for the new message (create new MOS in interaction containment, set covered property to destination lifeline, set message property)		
		MessageOccurrenceSpecification recvEventOccur = (MessageOccurrenceSpecification) interacion.createFragment(name+"Recv",UMLPackage.eINSTANCE.getMessageOccurrenceSpecification());
		recvEventOccur.getCovereds().add(destLifeline);
		recvEventOccur.setMessage(message);

		// Set send event and receive event of the new message
		message.setSendEvent(sendEventOccur);
		message.setReceiveEvent(recvEventOccur);

		// Create and set new signature/operation in destination class)
		org.eclipse.uml2.uml.Class p = (org.eclipse.uml2.uml.Class) interacion.getModel().getPackagedElement(destLifeline.getRepresents().getType().getName());
		Operation o = p.createOwnedOperation(name, null, null);
		message.setSignature(o);
		
		System.out.println("Created new message!");
	}
	
	public static void createStateInvariant(Interaction interaction, String name, Lifeline lifeline, String constraint) {	
		// Create new state invariant in interaction containment 
		StateInvariant si = (StateInvariant) interaction.createFragment(name, UMLPackage.eINSTANCE.getStateInvariant());
		si.getCovereds().add(lifeline);
		
		// Create new constraint in state invariant containment
		Constraint con = (Constraint) si.createInvariant(name+"Constraint", UMLPackage.eINSTANCE.getConstraint());
		
		// Create new specification in constraint containment
		LiteralString ls = (LiteralString) con.createSpecification(name+"Specification", null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(constraint);
		
		System.out.println("Created new StateInvariant!");
	}
}
