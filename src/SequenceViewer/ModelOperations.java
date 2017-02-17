package SequenceViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.CombinedFragment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionConstraint;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperand;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageKind;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.ProfileApplication;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.operations.ExecutionSpecificationOperations;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

/**
 * @author Patrick Glück
 *
 */
public class ModelOperations {

	/**
	 * Creates a new lifeline in the current interaction.
	 * @param interaction  the current interaction, not <code>null</code>
	 * @param lifelines  the list of lifelines currently present in the current interaction, <code>null</code> if there are no lifelines yet
	 * @param name  the user-selected name for the new instance of class represented by the new lifeline, <code>null</code> if there is no instance
	 * @param selClass  the class represented by the lifeline, not <code>null</code>
	 * @param insertAt  the position at which the new lifeline should be inserted in the lifelines list
	 */
	public static void createLifeline(Interaction interaction, EList<Lifeline> lifelines, String name, org.eclipse.uml2.uml.Class selClass, int insertAt) {
		// Create new property (instance) of selected class in interaction containment
		Property p = interaction.createOwnedAttribute(name, selClass, UMLPackage.eINSTANCE.getProperty());
		
		// Create new lifeline with selected name in interaction containment
		Lifeline l = UMLFactory.eINSTANCE.createLifeline();
		l.setName(name+"Lifeline");
		
		// Add the new lifeline at the specified location
		lifelines.add(insertAt, l);

		// Set represents property of the new lifeline
		l.setRepresents(p);
	}
	
	public static void editLifeline(Interaction interaction, Lifeline editLifeline, EList<Lifeline> lifelines, String name, org.eclipse.uml2.uml.Class selClass, Lifeline insertAt) {
		Property p = (Property) editLifeline.getRepresents();
		
		if (p.getType() != selClass) {
			p.setType(selClass);
		}
		if (!(p.getName().equals(name))) {
			p.setName(name);
			editLifeline.setName(name+"Lifeline");
		}
		if (editLifeline != insertAt) {
			lifelines.remove(editLifeline);
			lifelines.add(lifelines.indexOf(insertAt)+1, editLifeline);
		}
	}
	
	public static void deleteLifeline(Interaction interaction, Lifeline delLifeline) {
		EList<InteractionFragment> fragments = delLifeline.getCoveredBys();
		EList<InteractionFragment> interFragments = interaction.getFragments();
		for (InteractionFragment frag : fragments) {
			if(frag.eClass() != UMLPackage.eINSTANCE.getCombinedFragment() && frag.eClass() != UMLPackage.eINSTANCE.getInteractionOperand()) {
				if(frag.eClass() == UMLPackage.eINSTANCE.getMessageOccurrenceSpecification()) {
					Message m = ((MessageOccurrenceSpecification) frag).getMessage();
					deleteMessage(interaction, m);
				} else {
					interFragments.remove(frag);
				}				
			}
		}
		interaction.getOwnedAttributes().remove(delLifeline.getRepresents());
		interaction.getLifelines().remove(delLifeline);
	}
	
	public static void createMessage2(Interaction interaction, String name, Lifeline origLifeline, Lifeline destLifeline, MessageSort sort, MessageKind kind, MessageOccurrenceSpecification insert, ModelUtilities.InsertPoint insertPoint, Map<String, String> params, String[] returns) {
		// Create new call message in interaction containment
		Message callMessage = interaction.createMessage(name+"_Call");
		Message returnMessage = interaction.createMessage(name+"_Return");
		
		// Set messageSort property
		callMessage.setMessageSort(sort);
		returnMessage.setMessageSort(MessageSort.REPLY_LITERAL);
		
		EList<InteractionFragment> ifs = interaction.getFragments();
		
		
		ActionExecutionSpecification insertIsSOE = ModelUtilities.isExecutionStartOrEnd(origLifeline, insert);
		
		if(insertIsSOE != null) {
			int b = 0;		
			if (insert.isSend()) {
				if (insertPoint == ModelUtilities.InsertPoint.AFTER) {
					b = ifs.indexOf(insert.getMessage().getReceiveEvent())+1;
				} else {
					b = ifs.indexOf(insert);
				}
			} else {
				if (insertPoint == ModelUtilities.InsertPoint.AFTER) {
					b = ifs.indexOf(insertIsSOE)+1;
				} else {
					b = ifs.indexOf(insert.getMessage().getSendEvent());
				}
			}				
			
			MessageOccurrenceSpecification callSendEventOccur = createMOS(name+"CallSend", origLifeline, callMessage);				
			MessageOccurrenceSpecification callRecvEventOccur = createMOS(name+"CallRecv", destLifeline, callMessage);	
			
			callMessage.setSendEvent(callSendEventOccur);
			callMessage.setReceiveEvent(callRecvEventOccur);
			
			MessageOccurrenceSpecification returnSendEventOccur = createMOS(name+"ReturnSend", destLifeline, returnMessage);		
			MessageOccurrenceSpecification returnRecvEventOccur = createMOS(name+"ReturnRecv", origLifeline, returnMessage);
			
			returnMessage.setSendEvent(returnSendEventOccur);
			returnMessage.setReceiveEvent(returnRecvEventOccur);
			
			ActionExecutionSpecification origAcExSp = null;
			ExecutionOccurrenceSpecification origExOcSt = null;
			ExecutionOccurrenceSpecification origExOcE = null;
			
			if ((insert.isSend() && insertPoint == ModelUtilities.InsertPoint.AFTER) || (insert.isReceive() && insertPoint == ModelUtilities.InsertPoint.BEFORE)) {
				int countAcEx = 0;
				for(InteractionFragment f : origLifeline.getCoveredBys()) {
					if (f.eClass() == UMLPackage.eINSTANCE.getActionExecutionSpecification()) {
						countAcEx++;
					}
				}
				origAcExSp = UMLFactory.eINSTANCE.createActionExecutionSpecification();
				origAcExSp.getCovereds().add(origLifeline);
				origAcExSp.setName(origLifeline.getName()+"AcExSp"+countAcEx);
				origAcExSp.setStart(callSendEventOccur);
				origAcExSp.setFinish(returnRecvEventOccur);

				origExOcSt = UMLFactory.eINSTANCE.createExecutionOccurrenceSpecification();
				origExOcSt.setCovered(origLifeline);
				origExOcSt.setName(origLifeline.getName() + "ExOcSt"+countAcEx);
				origExOcSt.setExecution(origAcExSp);
				
				origExOcE = UMLFactory.eINSTANCE.createExecutionOccurrenceSpecification();
				origExOcE.setCovered(origLifeline);
				origExOcE.setName(origLifeline.getName() + "ExOcE"+countAcEx);
				origExOcE.setExecution(origAcExSp);				
			}
			
			int destCountAcEx = 0;
			for(InteractionFragment f : destLifeline.getCoveredBys()) {
				if (f.eClass() == UMLPackage.eINSTANCE.getActionExecutionSpecification()) {
					destCountAcEx++;
				}
			}
			ActionExecutionSpecification destAcExSp = UMLFactory.eINSTANCE.createActionExecutionSpecification();
			destAcExSp.getCovereds().add(destLifeline);
			destAcExSp.setName(destLifeline.getName()+"AcExSp"+destCountAcEx);
			destAcExSp.setStart(callRecvEventOccur);
			destAcExSp.setFinish(returnSendEventOccur);
			
			
			if ((insert.isSend() && insertPoint == ModelUtilities.InsertPoint.AFTER) || (insert.isReceive() && insertPoint == ModelUtilities.InsertPoint.BEFORE)) {
				ifs.add(b, origExOcSt);
				ifs.add(b+1, origAcExSp);
				ifs.add(b+2, callSendEventOccur);
				ifs.add(b+3, destAcExSp);
				ifs.add(b+4, callRecvEventOccur);
				ifs.add(b+5, returnSendEventOccur);
				ifs.add(b+6, returnRecvEventOccur);	
				ifs.add(b+7, origExOcE);		
			} else {
				ifs.add(b, callSendEventOccur);
				ifs.add(b+1, destAcExSp);
				ifs.add(b+2, callRecvEventOccur);
				ifs.add(b+3, returnSendEventOccur);
				ifs.add(b+4, returnRecvEventOccur);	
			}
			
		} else {
			int a = ifs.indexOf(insert); 
			if (insert.isReceive()) {
				a++;
			}
			
			MessageOccurrenceSpecification callSendEventOccur = createMOS(name+"CallSend", origLifeline, callMessage);		
			MessageOccurrenceSpecification callRecvEventOccur = createMOS(name+"CallRecv", destLifeline, callMessage);	
			
			callMessage.setSendEvent(callSendEventOccur);
			callMessage.setReceiveEvent(callRecvEventOccur);
			
			MessageOccurrenceSpecification returnSendEventOccur = createMOS(name+"ReturnSend", destLifeline, returnMessage);		
			MessageOccurrenceSpecification returnRecvEventOccur = createMOS(name+"ReturnRecv", origLifeline, returnMessage);
			
			returnMessage.setSendEvent(returnSendEventOccur);
			returnMessage.setReceiveEvent(returnRecvEventOccur);
			
			ActionExecutionSpecification acExSp = UMLFactory.eINSTANCE.createActionExecutionSpecification();
			acExSp.getCovereds().add(destLifeline);
			acExSp.setName(destLifeline.getName()+"AcExSp");
			acExSp.setStart(callRecvEventOccur);
			acExSp.setFinish(returnSendEventOccur);
			
			ifs.add(a, callSendEventOccur);
			ifs.add(a+1, acExSp);
			ifs.add(a+2, callRecvEventOccur);
			ifs.add(a+3, returnSendEventOccur);
			ifs.add(a+4, returnRecvEventOccur);		
			
		}
		
				
		// Create and set new signature/operation in destination class)
		org.eclipse.uml2.uml.Class p = (org.eclipse.uml2.uml.Class) interaction.getModel().getPackagedElement(destLifeline.getRepresents().getType().getName());
		Operation o = p.createOwnedOperation(name, null, null);
		Model currentModel = interaction.getModel();
		
		
		
		try {	
			org.eclipse.uml2.uml.Package package_ = null;	
			Resource resource = currentModel.eResource().getResourceSet().getResource(URI.createURI("pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml"), false);		
			package_ = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PACKAGE);
			if (params != null && params.size() > 0) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
				    if (ModelUtilities.isPrimitiveType(entry.getValue())) {
						o.createOwnedParameter(entry.getKey(), (Type) package_.getModel().getPackagedElement(entry.getValue()));
				    } else {
				    	o.createOwnedParameter(entry.getKey(), (Class)interaction.getModel().getPackagedElement(entry.getValue()));
				    }
				}
			}
			if (returns != null && returns.length == 2) {
				Parameter returnParam;
				if (ModelUtilities.isPrimitiveType(returns[1])) {
					returnParam = o.createOwnedParameter(returns[0], (Type) package_.getModel().getPackagedElement(returns[1]));
			    } else {
					returnParam = o.createOwnedParameter(returns[0], (Class)interaction.getModel().getPackagedElement(returns[1]));
			    }
				returnParam.setDirection(ParameterDirectionKind.RETURN_LITERAL);
				
			} 
		} catch (WrappedException we) {
			we.printStackTrace();
			System.exit(1);
		}
		
		callMessage.setSignature(o);
		returnMessage.setSignature(o);

		
	}
	
	
	public static void editMessage(Interaction interaction, String name, Lifeline origLifeline, Lifeline destLifeline, MessageSort sort, MessageKind kind, MessageOccurrenceSpecification insert, Message editMessage) {
		if(name != editMessage.getName()) {
			editMessage.setName(name);
		}
		if(sort != editMessage.getMessageSort()) {
			editMessage.setMessageSort(sort);
		}
	}
	
	public static void deleteMessage(Message delMessage) {
		Interaction inter = delMessage.getInteraction();
		EList<InteractionFragment> interFragments = inter.getFragments();
		EList<Message> interMsgs = inter.getMessages();
		MessageOccurrenceSpecification se = (MessageOccurrenceSpecification) delMessage.getSendEvent();
		MessageOccurrenceSpecification re = (MessageOccurrenceSpecification) delMessage.getReceiveEvent();
		ActionExecutionSpecification ac = ModelUtilities.isExecutionStartOrEnd(re.getCovered(), re);
		MessageOccurrenceSpecification se2 = (MessageOccurrenceSpecification) ac.getFinish();
		Message delMessage2 = se2.getMessage();
		MessageOccurrenceSpecification re2 = (MessageOccurrenceSpecification) delMessage2.getReceiveEvent();
		Operation o = (Operation) delMessage.getSignature();
		o.getClass_().getOwnedOperations().remove(o);
		interFragments.remove(se);
		interFragments.remove(re);
		interFragments.remove(se2);
		interFragments.remove(re2);
		interFragments.remove(ac);
		interMsgs.remove(delMessage);
		interMsgs.remove(delMessage2);
		
	}
	
	public static void createStateInvariant(Interaction interaction, String name, Lifeline lifeline, String usrConstraint) {	
		// Create new state invariant at the beginning of the interaction containment with name and lifeline as covered
		StateInvariant si = UMLFactory.eINSTANCE.createStateInvariant();
		si.setName(name);
		si.getCovereds().add(lifeline);
		interaction.getFragments().add(0, si);
		
		// Create new constraint in state invariant containment
		Constraint con = (Constraint) si.createInvariant(name+"Constraint", UMLPackage.eINSTANCE.getConstraint());
		
		// Create new specification in constraint containment with value usrConstraint
		LiteralString ls = (LiteralString) con.createSpecification(name+"Specification", null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(usrConstraint);
		
		System.out.println("Created new state invariant!");
	}
	
	public static void editStateInvariant(Interaction interaction, String name, Lifeline lifeline, String constraint, StateInvariant i) {
		if (name != i.getName()) {
			i.setName(name);
			i.getInvariant().setName(name+"Constraint");
			i.getInvariant().getSpecification().setName(name+"Specification");
		}
		if (lifeline != i.getCovereds().get(0)) {
			i.getCovereds().remove(0);
			i.getCovereds().add(lifeline);
		}
		if (!(constraint.equals(((LiteralString)i.getInvariant().getSpecification()).getValue()))) {
			((LiteralString)i.getInvariant().getSpecification()).setValue(constraint);			
		}	
	}
	
	public static void deleteStateInvariant(Interaction interaction, StateInvariant delInvariant) {
		interaction.getFragments().remove(delInvariant);
	}
	
	public static void createCombinedFragment(Interaction interaction, String name, InteractionOperatorKind type, Lifeline[] lifelines, String constraint) {
		
		CombinedFragment cf = (CombinedFragment) interaction.createFragment(name, UMLPackage.eINSTANCE.getCombinedFragment());
		cf.setInteractionOperator(type);
		InteractionOperand io = cf.createOperand(name+"Operand");
		EList<Lifeline> cfCovereds = cf.getCovereds();
		EList<Lifeline> ioCovereds = io.getCovereds();
		for (Lifeline l : lifelines) {
			cfCovereds.add(l);
			ioCovereds.add(l);
		}
		InteractionConstraint g = io.createGuard(name+"Guard");
		LiteralString ls = (LiteralString) g.createSpecification(name+"Specification", null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(constraint);
	}
	
	public static void editCombinedFragment(Interaction interaction, String name, InteractionOperatorKind type, Lifeline[] lifelines, String constraint, CombinedFragment editFragment) {
		if (name != editFragment.getName()) {
			editFragment.setName(name);
			editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification().setName(name+"Specification");
			editFragment.getOperand(editFragment.getName()+"Operand").getGuard().setName(name);
			editFragment.getOperand(editFragment.getName()+"Operand").setName(name+"Operand");
		}
		if (type != editFragment.getInteractionOperator()) {
			editFragment.setInteractionOperator(type);
		}
		if (!((LiteralString)editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification()).getValue().equals(constraint)) {
			((LiteralString)editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification()).setValue(constraint);
		}
	}
	
	public static void createInteractionOperand(CombinedFragment cf, String constraint) {
		EList<InteractionOperand> operands = cf.getOperands();
		InteractionOperand iop = UMLFactory.eINSTANCE.createInteractionOperand();
		iop.setName(cf.getName() + "Operand" + operands.size());
		InteractionConstraint g = iop.createGuard(cf.getName() + "Guard" + operands.size());
		LiteralString ls = (LiteralString) g.createSpecification(cf.getName() + "Specification" + operands.size(), null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(constraint);
		operands.add(iop);
	}
	
	private static MessageOccurrenceSpecification createMOS(String name, Lifeline covers, Message msg) {
		MessageOccurrenceSpecification mos = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		mos.setName(name);
		mos.getCovereds().add(covers);
		mos.setMessage(msg);
		return mos;
	}

	private static void deleteMessage(Interaction inter, Message msg) {
		MessageOccurrenceSpecification se = (MessageOccurrenceSpecification) msg.getSendEvent();
		MessageOccurrenceSpecification re = (MessageOccurrenceSpecification) msg.getReceiveEvent();
		Operation o = (Operation) msg.getSignature();		
		if(o != null) {
			Class p = (Class) o.getOwner();
			if (p != null && p.getOwnedOperations() != null) {
				boolean opop = p.getOwnedOperations().remove(o);
			}
		}		
		inter.getFragments().remove(se);
		inter.getFragments().remove(re);
		inter.getMessages().remove(msg);
	}
	
	
}
