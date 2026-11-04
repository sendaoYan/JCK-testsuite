/*
 * @test
 * @summary javax.swing.plaf.basic.BasicTextUI.BasicCaret conformance: white-box structure + black-box behavior.
 * @run main BasicCaretTest
 */
import java.lang.reflect.Modifier;

public class BasicCaretTest {
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicTextUI$BasicCaret");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.DefaultCaret"), "superclass javax.swing.text.DefaultCaret");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");

    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicTextUI.BasicCaret caret = new javax.swing.plaf.basic.BasicTextUI.BasicCaret();
        check(caret instanceof javax.swing.plaf.UIResource, "UIResource marker");
        check(caret instanceof javax.swing.text.Caret, "Caret");
        check(caret.getBlinkRate() >= 0, "default blink rate non-negative");
        caret.setBlinkRate(250);
        check(caret.getBlinkRate() == 250, "blink rate round-trip");
    }

}
