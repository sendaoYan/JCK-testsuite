/*
 * @test
 * @summary javax.swing.plaf.metal.MetalComboBoxEditor.UIResource conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main UIResourceTest
 */
import java.lang.reflect.Modifier;

public class UIResourceTest {
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
        runOnEDT(UIResourceTest::testWhiteBox);
        runOnEDT(UIResourceTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalComboBoxEditor$UIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.metal.MetalComboBoxEditor"), "superclass javax.swing.plaf.metal.MetalComboBoxEditor");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");

    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.metal.MetalComboBoxEditor.UIResource e = new javax.swing.plaf.metal.MetalComboBoxEditor.UIResource();
        check(e instanceof javax.swing.plaf.UIResource, "UIResource marker");
        e.setItem("x");
        check("x".equals(e.getItem()), "setItem/getItem round-trip");
    }

}
