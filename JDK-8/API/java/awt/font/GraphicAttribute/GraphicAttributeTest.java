/*
 * @test
 * @summary java.awt.font.GraphicAttribute conformance: abstract class reflection.
 * @run main GraphicAttributeTest
 */

public class GraphicAttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.font.GraphicAttribute");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAscent"), "method getAscent");
        check(hasMethod(c, "getDescent"), "method getDescent");
        check(hasMethod(c, "getAdvance"), "method getAdvance");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getOutline"), "method getOutline");
        check(hasMethod(c, "draw"), "method draw");
        check(hasMethod(c, "getAlignment"), "method getAlignment");
        check(hasMethod(c, "getJustificationInfo"), "method getJustificationInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
