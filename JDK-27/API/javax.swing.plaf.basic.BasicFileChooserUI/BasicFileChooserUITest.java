/*
 * @test
 * @summary javax.swing.plaf.basic.BasicFileChooserUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BasicFileChooserUITest
 */

import java.lang.reflect.Modifier;
import java.awt.EventQueue;

public class BasicFileChooserUITest {
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

    static Object reflectNew(String className, Class<?>[] paramTypes, Object... args) throws Exception {
        Class<?> c = Class.forName(className);
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(paramTypes);
        ctor.setAccessible(true);
        return ctor.newInstance(args);
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BasicFileChooserUITest::testWhiteBox);
        runOnEDT(BasicFileChooserUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicFileChooserUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.FileChooserUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        javax.swing.plaf.basic.BasicFileChooserUI ui = new javax.swing.plaf.basic.BasicFileChooserUI(fc);
        check(javax.swing.plaf.basic.BasicFileChooserUI.createUI(fc) instanceof javax.swing.plaf.basic.BasicFileChooserUI, "createUI");
        ui.installUI(fc);
        check(ui.getFileChooser() == fc, "getFileChooser");
        check(ui.getAccessoryPanel() != null, "accessory panel");
        check(ui.getApproveButtonText(fc) != null, "approve text");
        check(ui.getApproveButtonMnemonic(fc) >= 0, "approve mnemonic");
        check(ui.getDialogTitle(fc) != null, "dialog title");
        check(ui.getAcceptAllFileFilter(fc) != null, "accept all filter");
        check(ui.getFileView(fc) != null, "file view");
        ui.clearIconCache();
        ui.uninstallUI(fc);
    }
}
