/*
 * @test
 * @summary javax.swing.plaf.synth.ColorType conformance: white-box structure + black-box behavior.
 * @run main ColorTypeTest
 */

import java.lang.reflect.Modifier;
import javax.swing.plaf.synth.ColorType;

public class ColorTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.ColorType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        check(ColorType.FOREGROUND != null, "FOREGROUND non-null");
        check(ColorType.BACKGROUND != null, "BACKGROUND non-null");
        check(ColorType.TEXT_FOREGROUND != null, "TEXT_FOREGROUND non-null");
        check(ColorType.TEXT_BACKGROUND != null, "TEXT_BACKGROUND non-null");
        check(ColorType.FOCUS != null, "FOCUS non-null");
        check(ColorType.MAX_COUNT == 5, "MAX_COUNT == 5");

        check(ColorType.FOREGROUND.getID() == 0, "FOREGROUND id 0");
        check(ColorType.BACKGROUND.getID() == 1, "BACKGROUND id 1");
        check(ColorType.TEXT_FOREGROUND.getID() == 2, "TEXT_FOREGROUND id 2");
        check(ColorType.TEXT_BACKGROUND.getID() == 3, "TEXT_BACKGROUND id 3");
        check(ColorType.FOCUS.getID() == 4, "FOCUS id 4");

        check("Foreground".equals(ColorType.FOREGROUND.toString()), "FOREGROUND toString");
        check("Background".equals(ColorType.BACKGROUND.toString()), "BACKGROUND toString");
        check("TextForeground".equals(ColorType.TEXT_FOREGROUND.toString()), "TEXT_FOREGROUND toString");
        check("TextBackground".equals(ColorType.TEXT_BACKGROUND.toString()), "TEXT_BACKGROUND toString");
        check("Focus".equals(ColorType.FOCUS.toString()), "FOCUS toString");

        check(ColorType.FOREGROUND != ColorType.BACKGROUND
              && ColorType.BACKGROUND != ColorType.TEXT_FOREGROUND
              && ColorType.TEXT_FOREGROUND != ColorType.TEXT_BACKGROUND
              && ColorType.TEXT_BACKGROUND != ColorType.FOCUS,
              "constants are distinct");

        ColorType custom = new ColorType("Custom") {};
        check(custom.getID() == ColorType.MAX_COUNT, "custom id == MAX_COUNT");
        check("Custom".equals(custom.toString()), "custom toString");

        boolean npe = false;
        try { new ColorType(null) {}; } catch (NullPointerException e) { npe = true; }
        check(npe, "null description NPE");
    }
}
