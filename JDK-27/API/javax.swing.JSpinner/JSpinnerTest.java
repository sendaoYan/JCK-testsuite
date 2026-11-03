/*
 * @test
 * @summary javax.swing.JSpinner conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JSpinnerTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class JSpinnerTest {
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
        runOnEDT(JSpinnerTest::testWhiteBox);
        runOnEDT(JSpinnerTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSpinner");
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
        JSpinner def = new JSpinner();
        check(def.getValue() instanceof Integer, "default value Integer");
        check(((Integer) def.getValue()).intValue() == 0, "default value 0");
        check(((Integer) def.getNextValue()).intValue() == 1, "default next 1");
        check(((Integer) def.getPreviousValue()).intValue() == -1, "default previous -1");
        check(def.getModel() != null, "default model non-null");
        check(def.getModel() instanceof SpinnerNumberModel, "default model number model");
        check(def.getEditor() instanceof JSpinner.DefaultEditor, "default editor DefaultEditor");

        SpinnerNumberModel num = new SpinnerNumberModel(5, 0, 10, 2);
        JSpinner sp = new JSpinner(num);
        check(sp.getModel() == num, "number model ctor");
        check(((Integer) sp.getValue()).intValue() == 5, "number value 5");
        check(((Integer) sp.getNextValue()).intValue() == 7, "number next 7");
        check(((Integer) sp.getPreviousValue()).intValue() == 3, "number previous 3");

        sp.setValue(Integer.valueOf(9));
        check(((Integer) sp.getValue()).intValue() == 9, "setValue 9");
        check(sp.getNextValue() == null, "next null at max edge");
        sp.setValue(Integer.valueOf(0));
        check(sp.getPreviousValue() == null, "previous null at min edge");

        boolean iae = false;
        try { sp.setValue("not-a-number"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setValue non-number IAE");

        SpinnerNumberModel other = new SpinnerNumberModel(100, 0, 1000, 5);
        sp.setModel(other);
        check(sp.getModel() == other, "setModel");
        check(((Integer) sp.getValue()).intValue() == 100, "new model value 100");

        JLabel customEditor = new JLabel("editor");
        sp.setEditor(customEditor);
        check(sp.getEditor() == customEditor, "setEditor/getEditor");

        JSpinner empty = new JSpinner();
        empty.commitEdit(); // no exception expected
        check(true, "commitEdit ok");
        check(empty.getEditor() != null, "editor non-null after commit");
    }
}
