/*
 * @test
 * @summary javax.swing.JOptionPane conformance: type shape via reflection (headless-safe)
 *          plus behavioral contract (Metal LaF, headless).
 * @run main JOptionPaneTest
 */

import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicOptionPaneUI;

public class JOptionPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown"); }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        shape();
        behavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JOptionPane", false, JOptionPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getValue"), "method getValue");
        check(names.contains("setValue"), "method setValue");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() {
        try { UIManager.setLookAndFeel(new MetalLookAndFeel0()); }
        catch (Exception e) { throw new AssertionError("Metal LaF: " + e); }

        // constants
        check(JOptionPane.ERROR_MESSAGE == 0, "ERROR_MESSAGE 0");
        check(JOptionPane.INFORMATION_MESSAGE == 1, "INFORMATION_MESSAGE 1");
        check(JOptionPane.WARNING_MESSAGE == 2, "WARNING_MESSAGE 2");
        check(JOptionPane.QUESTION_MESSAGE == 3, "QUESTION_MESSAGE 3");
        check(JOptionPane.PLAIN_MESSAGE == -1, "PLAIN_MESSAGE -1");
        check(JOptionPane.DEFAULT_OPTION == -1, "DEFAULT_OPTION -1");
        check(JOptionPane.YES_NO_OPTION == 0, "YES_NO_OPTION 0");
        check(JOptionPane.YES_NO_CANCEL_OPTION == 1, "YES_NO_CANCEL_OPTION 1");
        check(JOptionPane.OK_CANCEL_OPTION == 2, "OK_CANCEL_OPTION 2");
        check(JOptionPane.YES_OPTION == 0 && JOptionPane.OK_OPTION == 0, "YES_OPTION/OK_OPTION 0");
        check(JOptionPane.NO_OPTION == 1, "NO_OPTION 1");
        check(JOptionPane.CANCEL_OPTION == 2, "CANCEL_OPTION 2");
        check(JOptionPane.CLOSED_OPTION == -1, "CLOSED_OPTION -1");
        check("uninitializedValue".equals(JOptionPane.UNINITIALIZED_VALUE), "UNINITIALIZED_VALUE string");
        check("message".equals(JOptionPane.MESSAGE_PROPERTY), "MESSAGE_PROPERTY name");
        check("value".equals(JOptionPane.VALUE_PROPERTY), "VALUE_PROPERTY name");
        check("wantsInput".equals(JOptionPane.WANTS_INPUT_PROPERTY), "WANTS_INPUT_PROPERTY name");

        // defaults
        JOptionPane p = new JOptionPane();
        check(p.getMessageType() == JOptionPane.PLAIN_MESSAGE, "default messageType PLAIN(-1)");
        check(p.getOptionType() == JOptionPane.DEFAULT_OPTION, "default optionType DEFAULT(-1)");
        check(p.getValue() == JOptionPane.UNINITIALIZED_VALUE, "default value UNINITIALIZED_VALUE (identity)");
        check(p.getWantsInput() == false, "default wantsInput false");
        check(p.getOptions() == null, "default options null");
        check(p.getInitialValue() == null, "default initialValue null");
        check(p.getInitialSelectionValue() == null, "default initialSelectionValue null");
        check(p.getSelectionValues() == null, "default selectionValues null");
        check(p.getIcon() == null, "default icon null");
        check(p.getMaxCharactersPerLineCount() == Integer.MAX_VALUE, "maxCharactersPerLineCount MAX_VALUE");

        // setter roundtrips + null semantics
        p.setMessage("hello");
        check("hello".equals(p.getMessage()), "setMessage roundtrip");
        Object[] multi = {"a", "b"};
        p.setMessage(multi);
        check(p.getMessage() instanceof Object[], "setMessage(Object[]) preserved");
        p.setMessageType(JOptionPane.WARNING_MESSAGE);
        check(p.getMessageType() == JOptionPane.WARNING_MESSAGE, "setMessageType roundtrip");
        p.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        check(p.getOptionType() == JOptionPane.OK_CANCEL_OPTION, "setOptionType roundtrip");
        Object[] opts = {"Yes", "No"};
        p.setOptions(opts);
        check(p.getOptions().length == 2, "setOptions roundtrip");
        p.setOptions(null);
        check(p.getOptions() == null, "setOptions(null) -> null");
        p.setInitialValue("Yes");
        check("Yes".equals(p.getInitialValue()), "setInitialValue roundtrip");
        p.setValue("No");
        check("No".equals(p.getValue()), "setValue roundtrip");
        p.setValue(null);
        check(p.getValue() == null, "setValue(null) -> null");
        Object[] sels = {"A", "B"};
        p.setSelectionValues(sels);
        check(p.getSelectionValues().length == 2, "setSelectionValues roundtrip");
        check(p.getWantsInput(), "setSelectionValues implies wantsInput");
        p.setSelectionValues(null);
        check(p.getSelectionValues() == null, "setSelectionValues(null) -> null");
        p.setInitialSelectionValue("B");
        check("B".equals(p.getInitialSelectionValue()), "setInitialSelectionValue roundtrip");
        p.setWantsInput(false);
        check(!p.getWantsInput(), "setWantsInput(false)");
        p.setWantsInput(true);
        check(p.getWantsInput(), "setWantsInput(true) fires event below");

        // bound-property event names
        final List<String> names = new ArrayList<String>();
        p.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { names.add(e.getPropertyName()); }});
        p.setMessage("m2");
        check(names.contains("message"), "setMessage fires \"message\"");
        p.setMessageType(JOptionPane.ERROR_MESSAGE);
        check(names.contains("messageType"), "setMessageType fires \"messageType\"");
        p.setOptionType(JOptionPane.YES_NO_OPTION);
        check(names.contains("optionType"), "setOptionType fires \"optionType\"");
        p.setWantsInput(false);
        check(names.contains("wantsInput"), "setWantsInput fires \"wantsInput\"");
        p.setValue("v");
        check(names.contains("value"), "setValue fires \"value\"");

        // invalid types: RuntimeException (not IllegalArgumentException), value unchanged
        final JOptionPane q = new JOptionPane("m");
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { q.setMessageType(99); }});
        check(q.getMessageType() == JOptionPane.PLAIN_MESSAGE, "messageType unchanged after invalid set");
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { q.setOptionType(99); }});
        check(q.getOptionType() == JOptionPane.DEFAULT_OPTION, "optionType unchanged after invalid set");
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { new JOptionPane("m", 99); }});
        checkThrows(RuntimeException.class, new Runnable() {
            public void run() { new JOptionPane("m", JOptionPane.PLAIN_MESSAGE, 99); }});

        // 6-arg ctor: message, messageType, optionType, icon, options, initialValue
        Icon ic = new Icon() {
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {}
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
        JOptionPane six = new JOptionPane("m6", JOptionPane.ERROR_MESSAGE,
                JOptionPane.YES_NO_OPTION, ic, new Object[]{"x", "y"}, "x");
        check("m6".equals(six.getMessage()), "6-arg ctor message");
        check(six.getMessageType() == JOptionPane.ERROR_MESSAGE, "6-arg ctor messageType");
        check(six.getOptionType() == JOptionPane.YES_NO_OPTION, "6-arg ctor optionType");
        check(six.getIcon() == ic, "6-arg ctor icon");
        check(six.getOptions().length == 2, "6-arg ctor options");
        check("x".equals(six.getInitialValue()), "6-arg ctor initialValue");

        // headless: dialog creation, frame lookup, and the modal showInputDialog
        // throw HeadlessException. Under a display (the full -Xcomp run sets
        // DISPLAY) showInputDialog would block on a modal dialog, so gate the
        // whole headless contract on isHeadless().
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() { new JOptionPane().createDialog(null, "t"); }});
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() { new JOptionPane().createDialog("t"); }});
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() { JOptionPane.getFrameForComponent(null); }});
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() { JOptionPane.getFrameForComponent(new JPanel()); }});
            checkThrows(HeadlessException.class, new Runnable() {
                public void run() {
                    JOptionPane.showInputDialog(null, "q"); }});
        }

        // UI plumbing
        check(p.getUIClassID().equals("OptionPaneUI"), "getUIClassID");
        check(p.getUI() instanceof BasicOptionPaneUI, "Metal LaF installs BasicOptionPaneUI");
        p.updateUI();
        check(p.getUI() != null, "updateUI no-throw");
        check(p.getAccessibleContext() != null, "accessible context non-null");
    }

    static class MetalLookAndFeel0 extends javax.swing.plaf.metal.MetalLookAndFeel {}
}
