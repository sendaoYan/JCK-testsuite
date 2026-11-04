/*
 * @test
 * @summary javax.swing.plaf.metal.MetalComboBoxEditor conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalComboBoxEditorTest
 */
import java.lang.reflect.Modifier;

public class MetalComboBoxEditorTest {
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
        runOnEDT(MetalComboBoxEditorTest::testWhiteBox);
        runOnEDT(MetalComboBoxEditorTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalComboBoxEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicComboBoxEditor"), "superclass javax.swing.plaf.basic.BasicComboBoxEditor");
        check(c.getInterfaces().length == 0, "no direct interfaces");

    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.metal.MetalComboBoxEditor e = new javax.swing.plaf.metal.MetalComboBoxEditor();
        check(e.getEditorComponent() != null, "editor component non-null");
        e.setItem("hello");
        check("hello".equals(e.getItem()), "setItem/getItem round-trip");
        e.selectAll();
        check("hello".equals(e.getItem()), "selectAll keeps text");
    }

}
