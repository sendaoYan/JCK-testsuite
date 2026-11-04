/*
 * @test
 * @summary javax.swing.JOptionPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JOptionPaneTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class JOptionPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JOptionPaneTest::testWhiteBox);
        runOnEDT(JOptionPaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JOptionPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        check(JOptionPane.DEFAULT_OPTION == -1, "DEFAULT_OPTION -1");
        check(JOptionPane.YES_NO_OPTION == 0, "YES_NO_OPTION 0");
        check(JOptionPane.YES_NO_CANCEL_OPTION == 1, "YES_NO_CANCEL_OPTION 1");
        check(JOptionPane.OK_CANCEL_OPTION == 2, "OK_CANCEL_OPTION 2");
        check(JOptionPane.YES_OPTION == 0, "YES_OPTION 0");
        check(JOptionPane.NO_OPTION == 1, "NO_OPTION 1");
        check(JOptionPane.CANCEL_OPTION == 2, "CANCEL_OPTION 2");
        check(JOptionPane.OK_OPTION == 0, "OK_OPTION 0");
        check(JOptionPane.CLOSED_OPTION == -1, "CLOSED_OPTION -1");
        check(JOptionPane.ERROR_MESSAGE == 0, "ERROR_MESSAGE 0");
        check(JOptionPane.INFORMATION_MESSAGE == 1, "INFORMATION_MESSAGE 1");
        check(JOptionPane.WARNING_MESSAGE == 2, "WARNING_MESSAGE 2");
        check(JOptionPane.QUESTION_MESSAGE == 3, "QUESTION_MESSAGE 3");
        check(JOptionPane.PLAIN_MESSAGE == -1, "PLAIN_MESSAGE -1");
        check("uninitializedValue".equals(JOptionPane.UNINITIALIZED_VALUE), "UNINITIALIZED_VALUE");
        check("message".equals(JOptionPane.MESSAGE_PROPERTY), "MESSAGE_PROPERTY");
        check("icon".equals(JOptionPane.ICON_PROPERTY), "ICON_PROPERTY");
        check("value".equals(JOptionPane.VALUE_PROPERTY), "VALUE_PROPERTY");
        check("options".equals(JOptionPane.OPTIONS_PROPERTY), "OPTIONS_PROPERTY");
        check("wantsInput".equals(JOptionPane.WANTS_INPUT_PROPERTY), "WANTS_INPUT_PROPERTY");

        JOptionPane p = new JOptionPane();
        check("JOptionPane message".equals(p.getMessage()), "default message");
        check(p.getMessageType() == JOptionPane.PLAIN_MESSAGE, "default messageType PLAIN");
        check(p.getOptionType() == JOptionPane.DEFAULT_OPTION, "default optionType DEFAULT");
        check(p.getValue() == JOptionPane.UNINITIALIZED_VALUE, "default value uninitialized");
        check(p.getInputValue() == JOptionPane.UNINITIALIZED_VALUE, "default inputValue uninitialized");
        check(p.getWantsInput() == false, "default wantsInput false");
        check(p.getIcon() == null, "default icon null");
        check(p.getOptions() == null, "default options null");
        check(p.getMaxCharactersPerLineCount() == Integer.MAX_VALUE, "max chars MAX_VALUE");

        JOptionPane typed = new JOptionPane("m", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_OPTION);
        check("m".equals(typed.getMessage()), "typed message");
        check(typed.getMessageType() == JOptionPane.ERROR_MESSAGE, "typed messageType");
        check(typed.getOptionType() == JOptionPane.YES_NO_OPTION, "typed optionType");

        p.setMessage("hello");
        check("hello".equals(p.getMessage()), "setMessage");
        p.setMessageType(JOptionPane.WARNING_MESSAGE);
        check(p.getMessageType() == JOptionPane.WARNING_MESSAGE, "setMessageType");
        p.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        check(p.getOptionType() == JOptionPane.OK_CANCEL_OPTION, "setOptionType");

        Icon ic = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {}
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
        p.setIcon(ic);
        check(p.getIcon() == ic, "setIcon/getIcon");

        Object[] opts = {"Yes", "No"};
        p.setOptions(opts);
        check(p.getOptions() != null, "getOptions non-null");
        check(Arrays.equals(opts, p.getOptions()), "getOptions content equal");
        check(p.getOptions() != opts, "getOptions defensive copy");

        p.setInitialValue("Yes");
        check("Yes".equals(p.getInitialValue()), "setInitialValue");
        p.setValue("No");
        check("No".equals(p.getValue()), "setValue");

        Object[] sels = {"A", "B"};
        p.setSelectionValues(sels);
        check(Arrays.equals(sels, p.getSelectionValues()), "selection values content");
        check(p.getWantsInput() == true, "selection values imply wantsInput");
        p.setInitialSelectionValue("B");
        check("B".equals(p.getInitialSelectionValue()), "initial selection value");
        p.setInputValue("A");
        check("A".equals(p.getInputValue()), "input value");
        p.setWantsInput(false);
        check(p.getWantsInput() == false, "wantsInput off");

        Frame root = JOptionPane.getRootFrame();
        check(root != null, "getRootFrame non-null");
        check(JOptionPane.getFrameForComponent(null) == root, "getFrameForComponent(null) root");
        check(JOptionPane.getFrameForComponent(new JDesktopPane()) == root, "getFrameForComponent walks to root");
        check(JOptionPane.getDesktopPaneForComponent(null) == null, "getDesktopPaneForComponent(null) null");
        JDesktopPane desk = new JDesktopPane();
        check(JOptionPane.getDesktopPaneForComponent(desk) == desk, "getDesktopPaneForComponent desk");

        JDialog d1 = p.createDialog("title1");
        check(d1 != null, "createDialog(String) non-null");
        check("title1".equals(d1.getTitle()), "createDialog(String) title");
        check(d1.isVisible() == false, "createDialog not visible");
        d1.dispose();

        JDialog d2 = p.createDialog(null, "title2");
        check(d2 != null, "createDialog(Component,String) non-null");
        check("title2".equals(d2.getTitle()), "createDialog(Component,String) title");
        check(d2.isVisible() == false, "createDialog component not visible");
        d2.dispose();
    }
}
