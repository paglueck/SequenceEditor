package SequenceViewer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.ActionExecutionSpecification;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.InteractionOperatorKind;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Class providing various utility functions for uml elements, enums and more
 *
 */
public class ModelUtilities {

    /**
     * Enum used for inidicating creation or editing.
     */
    public enum DialogOps {
        CREATE, EDIT
    }

    /**
     * Enum used for indicating insertion after or before an element.
     */
    public enum InsertPoint {
        AFTER, BEFORE
    }

    /**
     * Converts values of an {@link Object} array into an string array.
     * 
     * @param values
     *            the values that should be returned as string array, not <code>null</code>
     * @return the string array containing the values as strings, not <code>null</code>
     */
    public static String[] valuesToStrings(final Object[] values) {
        final String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = values[i].toString();
        }
        return strings;
    }

    /**
     * Converts a {@link Lifeline} array to a string array containing the names of the
     * {@link Lifeline}s.
     * 
     * @param lifelines
     *            the array of {@link Lifeline}s, not <code>null</code>
     * @return the string array containing the {@link Lifeline} names, not <code>null</code>
     */
    public static String[] lifelinesToStrings(final Lifeline[] lifelines) {
        final String[] strLifelines = new String[lifelines.length];
        for (int i = 0; i < lifelines.length; i++) {
            strLifelines[i] = lifelines[i].getName();
        }
        return strLifelines;
    }

    /**
     * Converts a {@link Message} array to a string array containing the names of the
     * {@link Message}s.
     * 
     * @param messages
     *            the array containing the {@link Message}s, not <code>null</code>
     * @return the string array containing the names of the {@link Message}s, not <code>null</code>
     */
    public static String[] messagesToStrings(final Object[] messages) {
        final String[] strMessages = new String[messages.length];
        for (int i = 0; i < messages.length; i++) {
            final Message m = (Message) messages[i];
            strMessages[i] = m.getName();
        }
        return strMessages;
    }

    /**
     * Returns a default action listener for a cancel button.
     * 
     * @return the action listener with the dispose functionality, not <code>null</code>
     */
    public static ActionListener getCancelActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // Disposes the button's window
                SwingUtilities.getWindowAncestor((JButton) e.getSource()).dispose();
            }
        };
    }

    /**
     * Concatenates two string arrays.
     * 
     * @param a
     *            the first string array, not <code>null</code>
     * @param b
     *            the second string array, not <code>null</code>
     * @return the concatenated string array, not <code>null</code>
     */
    public static String[] concat(final String[] a, final String[] b) {
        final int aLen = a.length;
        final int bLen = b.length;
        final String[] c = new String[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Returns the center position for a given window.
     * 
     * @param frame
     *            the window for which the center position should be calculated, not
     *            <code>null</code>
     * @return the center position for the window as {@link Point} object, not <code>null</code>
     */
    public static Point getCenterPosition(final Window frame) {
        final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        final int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        return new Point(x, y);
    }

    /**
     * Gets the {@link InteractionOperatorKind} to a given string.
     * 
     * @param iop
     *            the string representing a {@link InteractionOperatorKind} literal, not
     *            <code>null</code>
     * @return the {@link InteractionOperatorKind} represented by the string, not <code>null</code>
     */
    public static InteractionOperatorKind getInteractionOperatorKind(final String iop) {
        switch (iop) {
        case "Alt":
            return InteractionOperatorKind.ALT_LITERAL;
        case "Opt":
            return InteractionOperatorKind.OPT_LITERAL;
        case "Par":
            return InteractionOperatorKind.PAR_LITERAL;
        case "Crit":
            return InteractionOperatorKind.CRITICAL_LITERAL;
        case "Loop":
            return InteractionOperatorKind.LOOP_LITERAL;
        default:
            return InteractionOperatorKind.ALT_LITERAL;
        }
    }

    /**
     * Returns a {@link Lifeline} array with all the selected in a given {@link Lifeline} array.
     * 
     * @param allLifelines
     *            the {@link Lifeline} array containing "all" {@link Lifeline}s, not
     *            <code>null</code>
     * @param selections
     *            the int array containing the indices of the selected {@link Lifeline} in the
     *            allLifelines array, not <code>null</code>
     * @return the {@link Lifeline} array containing all the selected {@link Lifeline}s, not
     *         <code>null</code>
     */
    public static Lifeline[] getSelectedLifelines(final Lifeline[] allLifelines, final int[] selections) {
        final Lifeline[] lifelines = new Lifeline[selections.length];
        for (int i = 0; i < selections.length; i++) {
            lifelines[i] = allLifelines[selections[i]];
        }
        return lifelines;
    }

    /**
     * Returns a list of {@link Message}s that are relevant to a given lifeline.
     * 
     * @param messages
     *            the {@link EList} containing all existing {@link Message}s, not <code>null</code>
     * @param lifeline
     *            the {@link Lifeline} for which the relevant {@link Message}s should get
     *            calculated, not <code>null</code>
     * @return the list containing the {@link Message}s that have a
     *         {@link MessageOccurrenceSpecification} that is associated with the {@link Lifeline},
     *         not <code>null</code>
     */
    public static List<Message> getRelevantMessages(final EList<Message> messages, final Lifeline lifeline) {
        final List<Message> filteredMessages = new ArrayList<Message>();
        for (final Message m : messages) {
            final MessageOccurrenceSpecification smos = (MessageOccurrenceSpecification) m.getSendEvent();
            final MessageOccurrenceSpecification rmos = (MessageOccurrenceSpecification) m.getReceiveEvent();
            if (smos.getCovered() == lifeline || rmos.getCovered() == lifeline) {
                filteredMessages.add(m);
            }
        }
        return filteredMessages;
    }

    /**
     * Splits a string containing parameter name / type pairs into a {@link Map}.
     * 
     * @param paramString
     *            the string containing the pairs, not <code>null</code>
     * @return the map containing the parameter name / type pairs, where the key represents the name
     *         of the parameter and the value the type of the parameter, not <code>null</code>
     */
    public static LinkedHashMap<String, String> getParameters(final String paramString) {
        final LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
        final String[] paramPairs = paramString.split(";");
        for (final String p : paramPairs) {
            final String[] s = p.split(":");
            if (s.length == 2) {
                parameters.put(s[0], s[1]);
            } else {
                return null;
            }
        }
        return parameters;
    }

    /**
     * Returns a string array with the name and type of a return parameter for a given string.
     * 
     * @param returnString
     *            the string containing the return parameter name / type pair, not <code>null</code>
     * @return the string array, not <code>null</code>
     */
    public static String[] getReturn(final String returnString) {
        return returnString.split(":");
    }

    /**
     * Returns true if the given string corresponds to a known primitive type.
     * 
     * @param check
     *            the string to be checked, not <code>null</code>
     * @return true if the string corresponds to a known primitive type, else false, not
     *         <code>null</code>
     */
    public static boolean isPrimitiveType(final String check) {
        switch (check) {
        case "String":
            return true;
        case "Boolean":
            return true;
        case "Integer":
            return true;
        default:
            return false;
        }
    }

    /**
     * Checks if a given {@link MessageOccurrenceSpecification} is a start or finish point on any
     * {@link ActionExecutionSpecification} for a given {@link Lifeline}.
     * 
     * @param l
     *            the {@link Lifeline} to check for, not <code>null</code>
     * @param mos
     *            the {@link MessageOccurrenceSpecification} to check for, not <code>null</code>
     * @return the {@link ActionExecutionSpecification} if the
     *         {@link MessageOccurrenceSpecification} is a start or finish point, <code>null</code>
     *         else
     */
    public static ActionExecutionSpecification isExecutionStartOrEnd(final Lifeline l,
            final MessageOccurrenceSpecification mos) {
        final EList<InteractionFragment> lineFragments = l.getCoveredBys();
        for (final InteractionFragment f : lineFragments) {
            if (f.eClass() == UMLPackage.eINSTANCE.getActionExecutionSpecification()) {
                if (((ActionExecutionSpecification) f).getStart() == mos
                        || ((ActionExecutionSpecification) f).getFinish() == mos) {
                    return (ActionExecutionSpecification) f;
                }
            }
        }
        return null;
    }
}
