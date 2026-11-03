/*
 * @test
 * @summary javax.swing.plaf.metal.MetalFileChooserUI.FilterComboBoxRenderer conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FilterComboBoxRendererTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.metal.MetalFileChooserUI;

public class FilterComboBoxRendererTest {
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
        runOnEDT(FilterComboBoxRendererTest::testWhiteBox);
        runOnEDT(FilterComboBoxRendererTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$FilterComboBoxRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.DefaultListCellRenderer"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalFileChooserUI ui = new MetalFileChooserUI(new JFileChooser());
        MetalFileChooserUI.FilterComboBoxRenderer r = ui.new FilterComboBoxRenderer();
        check(r instanceof DefaultListCellRenderer, "instanceof DefaultListCellRenderer");
        check(r instanceof ListCellRenderer, "instanceof ListCellRenderer");

        Component comp = r.getListCellRendererComponent(new JList<>(), "item", 0, false, false);
        check(comp != null, "renderer component non-null");
    }
}
