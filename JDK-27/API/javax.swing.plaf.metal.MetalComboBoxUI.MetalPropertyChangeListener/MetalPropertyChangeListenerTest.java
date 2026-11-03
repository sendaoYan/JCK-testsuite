/*
 * @test
 * @summary javax.swing.plaf.metal.MetalComboBoxUI.MetalPropertyChangeListener conformance: white-box structure + black-box behavior.
 * @run main MetalPropertyChangeListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import javax.swing.JComboBox;
import javax.swing.plaf.metal.MetalComboBoxUI;

public class MetalPropertyChangeListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalComboBoxUI$MetalPropertyChangeListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicComboBoxUI$PropertyChangeHandler"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalComboBoxUI ui = new MetalComboBoxUI();
        MetalComboBoxUI.MetalPropertyChangeListener l = ui.new MetalPropertyChangeListener();
        check(l instanceof PropertyChangeListener, "instanceof PropertyChangeListener");

        // The delegate is not installed on a combo box, so its editor is
        // still null. Passing a real JComboBox as the source reaches the
        // BasicComboBoxUI.PropertyChangeHandler's non-editor branch (source !=
        // null editor) where the source is cast to JComboBox; an unknown
        // property name then falls through every branch as a no-op, exercising
        // the cast path without dereferencing uninstalled UI state.
        PropertyChangeEvent evt = new PropertyChangeEvent(new JComboBox<>(), "jckTestProperty", null, null);
        assertNoThrow(() -> l.propertyChange(evt), "propertyChange benign property");
    }
}
