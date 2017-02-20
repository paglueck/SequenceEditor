package SequenceViewer;

import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
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
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.StateInvariant;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.profile.standard.Specification;

/**
 * Class providing methods for manipulating objects in a uml2 model.
 */
public class ModelOperations {

	/**
	 * Creates a new lifeline in the current interaction containment.
	 * @param interaction  the current interaction, not <code>null</code>
	 * @param lifelines  the list of lifelines currently present in the current interaction, <code>null</code> if there are no lifelines yet
	 * @param name  the user-selected name for the new instance of class represented by the new lifeline, <code>null</code> if there is no instance
	 * @param selClass  the class represented by the lifeline, not <code>null</code>
	 * @param insertAt  the position at which the new lifeline should be inserted in the lifelines list
	 */
	public static void createLifeline(Interaction interaction, EList<Lifeline> lifelines, String name, Class selClass, int insertAt) {
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
	
	
	/**
	 * Edits an existing {@link Lifeline} object and potentially changed properties.
	 * @param interaction  the {@link Interaction} object which holds the {@link Lifeline} object to edit, not <code>null</code>
	 * @param editLifeline  the Lifeline object to edit, not <code>null</code>
	 * @param lifelines  the containment reference which holds all existing {@link Lifeline} objects, not <code>null</code>
	 * @param name  the name for the representing property of the {@link Lifeline} object, if it equals the current property's name no changes will be made to the property/lifeline names, not <code>null</code>
	 * @param selClass  the class which the {@link Lifeline} object's property represents, not <code>null</code>
	 * @param insertAt  the {@link Lifeline} object after which the lifeline to edit should be placed, not <code>null</code>
	 */
	public static void editLifeline(Lifeline editLifeline, EList<Lifeline> lifelines, String name, Class selClass, Lifeline insertAt) {
		// Retrieve the Property object represented by the lifeline
	    Property p = (Property) editLifeline.getRepresents();

	    // Change the name of the property/lifeline if it has changed
        if (!(p.getName().equals(name))) {
            p.setName(name);
            editLifeline.setName(name+"Lifeline");
        }
        
        // Change the represented type of the property if it has changed
		if (p.getType() != selClass) {
			p.setType(selClass);
		}
		
		// Change the position of the lifeline if it has changed
		if (editLifeline != insertAt) {
		    lifelines.remove(editLifeline);
			lifelines.add(lifelines.indexOf(insertAt)+1, editLifeline);
		}
	}
	
	/**
	 * Deletes the specified Lifeline object and all associated elements (except CombinedFragment objects and InteractionOperand ojects).
	 * @param interaction  the interaction holding the lifeline and the elements, not <code>null</code>
	 * @param delLifeline  the lifeline that should be deleted, not <code>null</code>
	 */
	public static void deleteLifeline(Interaction interaction, Lifeline delLifeline) {
	    // Local variable declarations
		EList<InteractionFragment> lifelineFragments = delLifeline.getCoveredBys();
		EList<InteractionFragment> interFragments = interaction.getFragments();
		
		// For each fragment of the lifeline check if its a CombinedFragment/InteractionOperand, if its not delete it from the containment
		for (InteractionFragment frag : lifelineFragments) {
			if (frag != null) {
    		    if(frag.eClass() != UMLPackage.eINSTANCE.getCombinedFragment() && frag.eClass() != UMLPackage.eINSTANCE.getInteractionOperand()) {
    			    if(frag.eClass() == UMLPackage.eINSTANCE.getMessageOccurrenceSpecification()) {
    					Message m = ((MessageOccurrenceSpecification) frag).getMessage();
    					if (m != null) {
    					    deleteSingleMessage(m);
    					}    					
    				} else {
    					interFragments.remove(frag);
    				}				
    			}
			}
		}
		
		// Delete the property and the lifeline itself
		interaction.getOwnedAttributes().remove(delLifeline.getRepresents());
		interaction.getLifelines().remove(delLifeline);
		
		//TODO: Check the action execution specifications, if they still have start and finish, if not create execution occurrence specifications
	}
		
	/**
	 * Creates a new message and the corresponding message occurrence specifications in the current interaction containment.
	 * <p>
	 * If the chosen {@link MessageSort} literal is sync or asynch, the return message, message occurrence specifications and action execution specification will get generated too.
	 * 
	 * @param interaction  the current {@link Interaction} object, not <code>null</code>
	 * @param name  the name for the new {@link Message} object, chosen by the user, not <code>null</code>
	 * @param origLifeline  the {@link Lifeline} object from where the new message originates, not <code>null</code>
	 * @param destLifeline  the {@link Lifeline} object  at which the new message ends, not <code>null</code>
	 * @param sort  the {@link MessageSort} literal which represents the type of the new message, not <code>null</code>
	 * @param kind  the {@link MessageKind} literal which represents the kind of the new message, not <code>null</code>
	 * @param insert  the {@link MessageOccurrenceSpecification} object at which the new message should be inserted, not <code></code>  
	 * @param insertPoint  the {@link InsertPoint} enum which indicates whether the new message should be inserted after or before the selected message on the originating {@link Lifeline} object, not <code>null</code>  
	 * @param params  the {@link Map} object containing pairs of {@link String} objects representing the chosen parameters by the user, where the key is the name of the parameter and the value represents the type, is <code>null</code> if the user doesn't want any parameters
	 * @param returns  the {@link String} array containing the return parameter name and type as {@link String} objects, is <code>null</code> if the user doesn't want a return parameter, has always length 2 when not null
	 */
	public static void createMessage(Interaction interaction, String name, Lifeline origLifeline, Lifeline destLifeline, MessageSort sort, 
	            MessageKind kind, MessageOccurrenceSpecification insert, ModelUtilities.InsertPoint insertPoint, Map<String, String> params, String[] returns) {
	    
	    // Local variable declaration
        EList<InteractionFragment> interFragments = interaction.getFragments();
        ActionExecutionSpecification acExSpInsert = ModelUtilities.isExecutionStartOrEnd(origLifeline, insert);
	    
	    // Create the new message pair in the interaction containment
		Message callMessage = interaction.createMessage(name+"_Call");
		Message returnMessage = interaction.createMessage(name+"_Return");
		
		// Set messageSort properties for the new messages
		callMessage.setMessageSort(sort);
		returnMessage.setMessageSort(MessageSort.REPLY_LITERAL);	
		
		//TODO: Cases for lost/found messages and create/delete messages
		
		// acExSpInsert is not null if the insert message occurrence specification is a start or finish of an action execution specification on the originating lifeline
		if(acExSpInsert != null) {
		    
		    //  Count variable for inserting the newly created elements
			int b = 0;		
			
			// The position (b count variable) changes depending if the insert message occurence specification is a send event or a receive event and depending on the insertPoint enum value
			if (insert.isSend()) {
			    if (insertPoint == ModelUtilities.InsertPoint.AFTER) {
					b = interFragments.indexOf(insert.getMessage().getReceiveEvent())+1;
				} else {
					b = interFragments.indexOf(insert);
				}
			} else {
				if (insertPoint == ModelUtilities.InsertPoint.AFTER) {
					b = interFragments.indexOf(acExSpInsert)+1;
				} else {
					b = interFragments.indexOf(insert.getMessage().getSendEvent());
				}
			}				
			
			// Create the necessary message occurrence specifications for the new messages (2 for each message, send and receive event)
			MessageOccurrenceSpecification callSendEventOccur = createMOS(name+"CallSend", origLifeline, callMessage);				
			MessageOccurrenceSpecification callRecvEventOccur = createMOS(name+"CallRecv", destLifeline, callMessage);	
            MessageOccurrenceSpecification returnSendEventOccur = createMOS(name+"ReturnSend", destLifeline, returnMessage);        
            MessageOccurrenceSpecification returnRecvEventOccur = createMOS(name+"ReturnRecv", origLifeline, returnMessage);
			
            // Set the send event and receive event properties of the new messages
			callMessage.setSendEvent(callSendEventOccur);
			callMessage.setReceiveEvent(callRecvEventOccur);
			returnMessage.setSendEvent(returnSendEventOccur);
			returnMessage.setReceiveEvent(returnRecvEventOccur);
			
			// Declaration of an action execution and the corresponding execution occurrences if the new message pair doesn't get inserted on an existing action execution 
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

				origExOcSt = UMLFactory.eINSTANCE.createExecutionOccurrenceSpecification();
				origExOcSt.setCovered(origLifeline);
				origExOcSt.setName(origLifeline.getName() + "ExOcSt"+countAcEx);
				origExOcSt.setExecution(origAcExSp);
				
				origExOcE = UMLFactory.eINSTANCE.createExecutionOccurrenceSpecification();
				origExOcE.setCovered(origLifeline);
				origExOcE.setName(origLifeline.getName() + "ExOcE"+countAcEx);
				origExOcE.setExecution(origAcExSp);	

				//origAcExSp = createAES(origLifeline, origExOcSt, origExOcE, countAcEx);
                origAcExSp = UMLFactory.eINSTANCE.createActionExecutionSpecification();
                origAcExSp.getCovereds().add(origLifeline);
                origAcExSp.setName(origLifeline.getName()+"AcExSp"+countAcEx);
                origAcExSp.setStart(origExOcSt);
                origAcExSp.setFinish(origExOcE);
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
			    interFragments.add(b, origExOcSt);
			    interFragments.add(b+1, origAcExSp);
			    interFragments.add(b+2, callSendEventOccur);
			    interFragments.add(b+3, destAcExSp);
			    interFragments.add(b+4, callRecvEventOccur);
			    interFragments.add(b+5, returnSendEventOccur);
			    interFragments.add(b+6, returnRecvEventOccur);	
			    interFragments.add(b+7, origExOcE);		
			} else {
			    interFragments.add(b, callSendEventOccur);
			    interFragments.add(b+1, destAcExSp);
			    interFragments.add(b+2, callRecvEventOccur);
			    interFragments.add(b+3, returnSendEventOccur);
			    interFragments.add(b+4, returnRecvEventOccur);	
			}
			
		} else {
			int a = interFragments.indexOf(insert); 
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
			
			interFragments.add(a, callSendEventOccur);
			interFragments.add(a+1, acExSp);
			interFragments.add(a+2, callRecvEventOccur);
			interFragments.add(a+3, returnSendEventOccur);
			interFragments.add(a+4, returnRecvEventOccur);		
			
		}
		
				
		// Create and set new signature/operation in destination class)
		org.eclipse.uml2.uml.Class p = (org.eclipse.uml2.uml.Class) interaction.getModel().getPackagedElement(destLifeline.getRepresents().getType().getName());
		Operation o = p.createOwnedOperation(name, null, null);
		
		// Get the model, its associated resource set and the resource for the primitive types
		Model currentModel = interaction.getModel();			
		org.eclipse.uml2.uml.Package package_ = null;	
		Resource resource = currentModel.eResource().getResourceSet().getResource(URI.createURI("pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml"), false);		
		package_ = (org.eclipse.uml2.uml.Package) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.PACKAGE);
		
		// Create parameters for the operation, if there are parameters in the params map
		if (params != null && params.size() > 0) {
		    
		    // The map contains pairs of strings where the key represents the name of the parameter and the value represents the type of the parameter
			for (Map.Entry<String, String> entry : params.entrySet()) {
			    if (ModelUtilities.isPrimitiveType(entry.getValue())) {
			        
			        // Primitive type parameter
					o.createOwnedParameter(entry.getKey(), (Type) package_.getModel().getPackagedElement(entry.getValue()));
			    } else {
			        
			        // Class type parameter
			    	o.createOwnedParameter(entry.getKey(), (Class)interaction.getModel().getPackagedElement(entry.getValue()));
			    }
			}
		}
		
		// Create return parameter, if there is one in the returns array
		if (returns != null && returns.length == 2) {
			Parameter returnParam;
			if (ModelUtilities.isPrimitiveType(returns[1])) {
			    // Primitive type parameter
				returnParam = o.createOwnedParameter(returns[0], (Type) package_.getModel().getPackagedElement(returns[1]));
		    } else {
		        // Class type parameter
				returnParam = o.createOwnedParameter(returns[0], (Class)interaction.getModel().getPackagedElement(returns[1]));
		    }
			returnParam.setDirection(ParameterDirectionKind.RETURN_LITERAL);
		} 
		
		// Set the signature property of the messages
		callMessage.setSignature(o);
		returnMessage.setSignature(o);
	}
	
	
	/**
     * Edits an existing {@link Message} and the corresponding {@link MessageOccurrenceSpecification}s in the current {@link Interaction} containment.
     * 
     * @param interaction  the current {@link Interaction} object, not <code>null</code>
     * @param name  the name for the new {@link Message} object, chosen by the user, not <code>null</code>
     * @param origLifeline  the {@link Lifeline} object from where the new message originates, not <code>null</code>
     * @param destLifeline  the {@link Lifeline} object  at which the new message ends, not <code>null</code>
     * @param sort  the {@link MessageSort} literal which represents the type of the new message, not <code>null</code>
     * @param kind  the {@link MessageKind} literal which represents the kind of the new message, not <code>null</code>
     * @param insert  the {@link MessageOccurrenceSpecification} object at which the new message should be inserted, not <code>null</code>  
     * @param editMessage  the {@link Message} which should be edited, not <code>null</code>
     */
	public static void editMessage(Interaction interaction, String name, Lifeline origLifeline, Lifeline destLifeline, MessageSort sort, MessageKind kind, MessageOccurrenceSpecification insert, Message editMessage) {
		//TODO: Change position and kind
	    
	    // If the name variable differs from the message's current name, change to the new name
	    if(name != editMessage.getName()) {
			editMessage.setName(name);
		}
	    
	    // If the sort variable differs from the message's current message sort, change to the new message sort
		if(sort != editMessage.getMessageSort()) {
			editMessage.setMessageSort(sort);
		}
	}
	
	
	/**
	 * Deletes a {@link Message} pair from its {@link Interaction}'s containment.
	 * @param delMessage  the "root" {@link Message} of the pair
	 */
	public static void deleteMessage(Message delMessage) {
		
	    //TODO: Iteratively remove "child" elements
	    
	    // Local variable declaration
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
		
		// Remove the message's operation from its class
		o.getClass_().getOwnedOperations().remove(o);
		
		// Remove the occurrence specifications and action execution
		interFragments.remove(se);
		interFragments.remove(re);
		interFragments.remove(se2);
		interFragments.remove(re2);
		interFragments.remove(ac);
		
		// Remove the messages
		interMsgs.remove(delMessage);
		interMsgs.remove(delMessage2);
	}
	
	/**
	 * Creates a new {@link StateInvariant} object and the corresponding {@link Constraint} and {@link Specification} object.
	 * @param interaction  the current {@link Interaction} object, not <code>null</code>
	 * @param name  the name for the new {@link StateInvariant} object, not <code>null</code>
	 * @param lifeline  the {@link Lifeline} object with which the new {@link StateInvariant} object is associated with, not <code>null</code>
	 * @param usrConstraint  the constraint chosen by the user for the new {@link StateInvariant} object, not <code>null</code>
	 */
	public static void createStateInvariant(Interaction interaction, String name, Lifeline lifeline, String usrConstraint) {
	    
		// Create new state invariant at the beginning of the interaction containment and set necessary properties
		StateInvariant si = UMLFactory.eINSTANCE.createStateInvariant();
		si.setName(name);
		si.getCovereds().add(lifeline);
		interaction.getFragments().add(0, si);
		
		// Create new constraint in state invariant containment
		Constraint con = (Constraint) si.createInvariant(name+"Constraint", UMLPackage.eINSTANCE.getConstraint());
		
		// Create new specification in constraint containment with value usrConstraint
		LiteralString ls = (LiteralString) con.createSpecification(name+"Specification", null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(usrConstraint);		
	}
	
	/**
	 * Edits an already existing {@link StateInvariant}, changing possibly the name, covers and constraint of the state invariant.
	 * @param name  the name chosen by the user for the {@link StateInvariant}, not <code>null</code>
	 * @param lifeline  the {@link Lifeline} for which the {@link StateInvariant} should be associated with, not <code>null</code>
	 * @param constraint  the constraint entered by the user for the {@link StateInvariant}
	 * @param i
	 */
	public static void editStateInvariant(String name, Lifeline lifeline, String constraint, StateInvariant si) {
		
	    // Local variable declaration
	    EList<Lifeline> siCovered = si.getCovereds();
	    Constraint siConstraint = si.getInvariant();
		LiteralString siConValue = (LiteralString) siConstraint.getSpecification();
		
		// If the new name doesn't equal the old name, change to the new name
	    if (name != si.getName()) {
			si.setName(name);
			siConstraint.setName(name+"Constraint");
			siConValue.setName(name+"Specification");
		}		
		
	    // If the new lifeline doesn't equal the old lifeline, change to the new lifeline
		if (lifeline != siCovered.get(0)) {
			siCovered.remove(0);
			siCovered.add(lifeline);
		}
		
		// If the new constraint doesn't equal the old constraint, change to the new constraint
		if (!(constraint.equals(siConValue.getValue()))){
			siConValue.setValue(constraint);			
		}	
	}
	
	/**
	 * Deletes the specified {@link StateInvariant} object from the {@link Interaction} object's containment
	 * @param interaction  the {@link Interaction} object from which the {@link StateInvariant} object should be deleted
	 * @param delInvariant  the {@link StateInvariant} object which should be deleted
	 */
	public static void deleteStateInvariant(Interaction interaction, StateInvariant delInvariant) {
		
	    // Get fragments and remove the state invariant
	    interaction.getFragments().remove(delInvariant);
	}
	
	
	/**
	 * Create a new {@link CombinedFragment} object, the corresponding {@link InteractionOperand} and {@link InteractionConstraint} objects and set the necessary properties.
	 * @param interaction  the {@link Interaction} object which holds the containment for the new {@link CombinedFragment} object, not <code>null</code>
	 * @param name  the name chosen by the user for the new {@link CombinedFragment} object, not <code>null</code>
	 * @param type  the {@link InteractionOperatorKind} literal for the new {@link CombinedFragment} object, not <code>null</code>
	 * @param lifelines  the {@link Lifeline} objects which should cover the new {@link CombinedFragment} object, not <code>null</code>
	 * @param constraint  the constraint chosen by the user for the new {@link CombinedFragment} object's {@link InteractionOperand} object, not <code>null</code>
	 */
	public static void createCombinedFragment(Interaction interaction, String name, InteractionOperatorKind type, Lifeline[] lifelines, String usrConstraint) {
		
	    //TODO: Position for inserting the combined fragment
	    
	    // Create new combined fragment
		CombinedFragment cf = (CombinedFragment) interaction.createFragment(name, UMLPackage.eINSTANCE.getCombinedFragment());
		cf.setInteractionOperator(type);
		EList<Lifeline> cfCovereds = cf.getCovereds();
		
		// Create new interaction operand
		InteractionOperand io = cf.createOperand(name+"Operand");
		EList<Lifeline> ioCovereds = io.getCovereds();
		
		// Set the covered property for the combined fragment and interaction operand
		for (Lifeline l : lifelines) {
			cfCovereds.add(l);
			ioCovereds.add(l);
		}
		
		// Create new interaction constraint and set the value
		InteractionConstraint g = io.createGuard(name+"Guard");
		LiteralString ls = (LiteralString) g.createSpecification(name+"Specification", null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(usrConstraint);
	}	
	
	/**
	 * Edits an already existing {@link CombinedFragment} and sets possible changed properties.
	 * @param name  the possible new name for the {@link CombinedFragment}, not <code>null</code>
	 * @param type  the possible changed {@link InteractionOperatorKind} literal for the {@link CombinedFragment}, not <code>null</code>
	 * @param lifelines  the {@link Lifeline}s that are covered by the {@link CombinedFragment}, not <code>null</code>
	 * @param constraint  the possible changed constraint for the {@link CombinedFragment}'s {@link InteractionOperand}, not <code>null</code>
	 * @param editFragment  the {@link CombinedFragment} that subject to change, not <code>null</code> 
	 */
	public static void editCombinedFragment(String name, InteractionOperatorKind type, Lifeline[] lifelines, String constraint, CombinedFragment editFragment) {
		
	    // If the name variable differs from the combined fragment's current name, change all names associated with the combined fragment
	    if (name != editFragment.getName()) {
			editFragment.setName(name);
			editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification().setName(name+"Specification");
			editFragment.getOperand(editFragment.getName()+"Operand").getGuard().setName(name);
			editFragment.getOperand(editFragment.getName()+"Operand").setName(name+"Operand");
		}
	    
	    // If the type variable differs from the combined fragment's current type, change the type
		if (type != editFragment.getInteractionOperator()) {
			editFragment.setInteractionOperator(type);
		}
		
		// If the constraint variable differs from the combined fragment's interaction operand constraint, change the value to the new constraint
		if (!((LiteralString)editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification()).getValue().equals(constraint)) {
			((LiteralString)editFragment.getOperand(editFragment.getName()+"Operand").getGuard().getSpecification()).setValue(constraint);
		}
	}	
	
	/**
	 * Creates a new {@link InteractionOperand} Object and adds it to the given {@link CombinedFragment} object's containment. 
	 * @param cf  the {@link CombinedFragment} object for which the interaction operand should be created
	 * @param constraint  the constraint that must evaluate to true for the execution of the interaction operand's content, not <code>null</code>
	 */
	public static void createInteractionOperand(CombinedFragment cf, String constraint) {
	   
	    // Local variable declarations
		EList<InteractionOperand> operands = cf.getOperands();
		
		// Create new InteractionOperand object and set name (adds a number depending on the amount of interaction operands already existing)
		InteractionOperand iop = UMLFactory.eINSTANCE.createInteractionOperand();
		iop.setName(cf.getName() + "Operand" + operands.size());
		
		// Create guard for the new interaction operand and set its constraint value
		InteractionConstraint g = iop.createGuard(cf.getName() + "Guard" + operands.size());
		LiteralString ls = (LiteralString) g.createSpecification(cf.getName() + "Specification" + operands.size(), null, UMLPackage.eINSTANCE.getLiteralString());
		ls.setValue(constraint);
		
		// Add the new interaction operand to the CombinedFragment's containment
		operands.add(iop);
	}	
	
	/**
	 * Creates and returns a new {@link MessageOccurrenceSpecification} object and sets the required properties.
	 * @param name  the name for the new {@link MessageOccurrenceSpecification} object, not <code>null</code>
	 * @param covers  the {@link Lifeline} object which the new {@link MessageOccurrenceSpecification} object covers, not <code>null</code>
	 * @param msg  the {@link Message} object which the new {@link MessageOccurrenceSpecification} object is part of, not <code>null</code>
	 * @return the created {@link MessageOccurrenceSpecification} object
	 */
	private static MessageOccurrenceSpecification createMOS(String name, Lifeline covers, Message msg) {
		
	    // Local variable declarations	    
	    MessageOccurrenceSpecification mos = UMLFactory.eINSTANCE.createMessageOccurrenceSpecification();
		
	    // Set the required properties
	    mos.setName(name);
	    mos.getCovereds().add(covers);
	    mos.setMessage(msg);
	    
		return mos;
	}	
	
	/**
	 * Creates and returns a new {@link ActionExecutionSpecification} object and sets the required properties.
	 * @param lifeline  the {@link Lifeline} object which is covered by the new {@link ActionExecutionSpecification} object, not <code>null</code>
	 * @param start  the {@link OccurrenceSpecification} object which represents the start of the new {@link ActionExecutionSpecification} object, not <code>null</code>
	 * @param finish  the {@link OccurrenceSpecification} object which represents the end of the new {@link ActionExecutionSpecification} object, not <code>null</code>
	 * @param count  the amount of {@link ActionExecutionSpecification} objects already existing on the {@link Lifeline} object, not <code>null</code>
	 * @return  the new {@link ActionExecutionSpecification} object
	 */
	private static ActionExecutionSpecification createAES(Lifeline lifeline, OccurrenceSpecification start, OccurrenceSpecification finish, int count) {
	    
	    // Create new action execution specification
	    ActionExecutionSpecification acExSp = UMLFactory.eINSTANCE.createActionExecutionSpecification();
        
	    // Set the necessary properties
	    acExSp.getCovereds().add(lifeline);
        acExSp.setName(lifeline.getName()+"AcExSp"+count);
        acExSp.setStart(start);
        acExSp.setFinish(finish);
        
        return acExSp;
	}
	
	/**
	 * Deletes a single {@link Message} object and its associated {@link MessageOccurrenceSpecification} objects and {@link Operation} object.
	 * @param msg  the {@link Message} object that should be deleted, not <code>null</code>
	 */
	private static void deleteSingleMessage(Message msg) {
	    
	    // Local variable declarations
	    Interaction inter = msg.getInteraction();
	    EList<InteractionFragment> interFragments = inter.getFragments();
		MessageOccurrenceSpecification sendEvent = (MessageOccurrenceSpecification) msg.getSendEvent();
		MessageOccurrenceSpecification receiveEvent = (MessageOccurrenceSpecification) msg.getReceiveEvent();
		Operation o = (Operation) msg.getSignature();	
		
		// Delete operation associated with the message in the Class object
		if (o != null) {
		    if (o.getClass_() != null) {
    		    if(o.getClass_().getOperations() != null) {
    		        o.getClass_().getOwnedOperations().remove(o);
    		    }
		    }
		}	
				
		// Remove the message occurrence specifications from the interaction containment
		interFragments.remove(sendEvent);
		interFragments.remove(receiveEvent);
		
		// Remove the message from the interaction containment
		inter.getMessages().remove(msg);
	}	
}
