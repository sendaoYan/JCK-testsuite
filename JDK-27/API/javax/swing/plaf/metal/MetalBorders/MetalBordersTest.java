/*
 * @test
 * @summary javax.swing.plaf.metal.MetalBorders conformance: white-box structure + black-box behavior.
 * @run main MetalBordersTest
 */
import java.lang.reflect.Modifier;

public class MetalBordersTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalBorders");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "getButtonBorder"), "declares method getButtonBorder");
        check(hasDeclaredMethod(c, "getTextBorder"), "declares method getTextBorder");
        check(hasDeclaredMethod(c, "getTextFieldBorder"), "declares method getTextFieldBorder");
        check(hasDeclaredMethod(c, "getToggleButtonBorder"), "declares method getToggleButtonBorder");
        check(hasDeclaredMethod(c, "getDesktopIconBorder"), "declares method getDesktopIconBorder");
    }

    static void testBlackBox() throws Exception {
        check(javax.swing.plaf.metal.MetalBorders.getButtonBorder() != null, "button border");
        check(javax.swing.plaf.metal.MetalBorders.getTextBorder() != null, "text border");
        check(javax.swing.plaf.metal.MetalBorders.getTextFieldBorder() != null, "text field border");
        check(javax.swing.plaf.metal.MetalBorders.getToggleButtonBorder() != null, "toggle button border");
        check(javax.swing.plaf.metal.MetalBorders.getDesktopIconBorder() != null, "desktop icon border");
        new javax.swing.plaf.metal.MetalBorders();
    }

}
