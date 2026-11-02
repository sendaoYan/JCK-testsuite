/*
 * @test
 * @summary java.awt.color.ColorSpace conformance: abstract class reflection.
 * @run main ColorSpaceTest
 */

public class ColorSpaceTest {
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
        Class<?> c = Class.forName("java.awt.color.ColorSpace");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "isCS_sRGB"), "method isCS_sRGB");
        check(hasMethod(c, "toRGB"), "method toRGB");
        check(hasMethod(c, "fromRGB"), "method fromRGB");
        check(hasMethod(c, "toCIEXYZ"), "method toCIEXYZ");
        check(hasMethod(c, "fromCIEXYZ"), "method fromCIEXYZ");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getNumComponents"), "method getNumComponents");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getMinValue"), "method getMinValue");
        check(hasMethod(c, "getMaxValue"), "method getMaxValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
