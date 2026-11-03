/*
 * @test
 * @summary javax.swing.plaf.metal.MetalComboBoxButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalComboBoxButtonTest
 */
import java.lang.reflect.Modifier;

public class MetalComboBoxButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            java.awt.EventQueue.invokeAndWait(() -> {
                try { r.run(); } catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MetalComboBoxButtonTest::testWhiteBox);
        runOnEDT(MetalComboBoxButtonTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalComboBoxButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JButton"), "superclass javax.swing.JButton");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "getComboBox"), "declares method getComboBox");
        check(hasDeclaredMethod(c, "setComboBox"), "declares method setComboBox");
        check(hasDeclaredMethod(c, "getComboIcon"), "declares method getComboIcon");
        check(hasDeclaredMethod(c, "setComboIcon"), "declares method setComboIcon");
        check(hasDeclaredMethod(c, "isIconOnly"), "declares method isIconOnly");
        check(hasDeclaredMethod(c, "setIconOnly"), "declares method setIconOnly");
    }

    static void testBlackBox() throws Exception {
        javax.swing.JComboBox<Object> cb = new javax.swing.JComboBox<Object>();
        javax.swing.JList<Object> list = new javax.swing.JList<Object>();
        javax.swing.CellRendererPane pane = new javax.swing.CellRendererPane();
        javax.swing.plaf.metal.MetalComboBoxButton b = new javax.swing.plaf.metal.MetalComboBoxButton(cb, null, pane, list);
        check(b.getComboBox() == cb, "getComboBox round-trip");
        check(b.isIconOnly() == false, "not icon-only initially");
        b.setIconOnly(true);
        check(b.isIconOnly() == true, "icon-only round-trip");
        b.setComboIcon(new javax.swing.plaf.metal.MetalComboBoxIcon());
        check(b.getComboIcon() != null, "combo icon set");
        check(b.isFocusTraversable() == false, "not focus traversable");
        check(b.getMinimumSize() != null, "minimum size non-null");
    }

}
