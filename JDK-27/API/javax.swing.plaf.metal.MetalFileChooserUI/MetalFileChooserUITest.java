/*
 * @test
 * @summary javax.swing.plaf.metal.MetalFileChooserUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalFileChooserUITest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Modifier;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.metal.MetalFileChooserUI;

public class MetalFileChooserUITest {
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

    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MetalFileChooserUITest::testWhiteBox);
        runOnEDT(MetalFileChooserUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicFileChooserUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JFileChooser fc = new JFileChooser();
        MetalFileChooserUI ui = new MetalFileChooserUI(fc);
        check(MetalFileChooserUI.createUI(fc) instanceof MetalFileChooserUI, "createUI type");

        assertNoThrow(() -> ui.installUI(fc), "installUI");

        ui.setFileName("jck.txt");
        check("jck.txt".equals(ui.getFileName()), "fileName roundtrip");
        // getDirectoryName/setDirectoryName are stubs ("PENDING" in the JDK):
        // the setter is a no-op and the getter always returns null.
        assertNoThrow(() -> ui.setDirectoryName("."), "setDirectoryName");
        check(ui.getDirectoryName() == null, "getDirectoryName returns null");

        Dimension pref = ui.getPreferredSize(fc);
        Dimension min = ui.getMinimumSize(fc);
        Dimension max = ui.getMaximumSize(fc);
        check(pref != null && pref.width >= 0 && pref.height >= 0, "preferred size sane");
        check(min != null && min.width >= 0 && min.height >= 0, "minimum size sane");
        check(max != null && max.width >= 0 && max.height >= 0, "maximum size sane");

        check(ui.createListSelectionListener(fc) != null, "list selection listener");
        check(ui.createPropertyChangeListener(fc) != null, "property change listener");

        assertNoThrow(() -> ui.ensureFileIsVisible(fc, new File(".")), "ensureFileIsVisible");
        assertNoThrow(() -> ui.rescanCurrentDirectory(fc), "rescanCurrentDirectory");
        assertNoThrow(() -> ui.valueChanged(new ListSelectionEvent(new Object(), 0, 0, false)), "valueChanged");
        assertNoThrow(() -> ui.installComponents(fc), "installComponents");
        assertNoThrow(() -> ui.uninstallComponents(fc), "uninstallComponents");
        assertNoThrow(() -> ui.uninstallUI(fc), "uninstallUI");
    }
}
