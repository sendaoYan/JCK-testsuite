/*
 * @test
 * @summary com.sun.imageio.plugins.common.BogusColorSpace conformance: type shape via reflection (headless-safe).
 * @run main BogusColorSpaceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BogusColorSpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.common.BogusColorSpace", false, BogusColorSpaceTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.common.BogusColorSpace"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.color.ColorSpace"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("toRGB"), "method toRGB");
        check(names.contains("fromRGB"), "method fromRGB");
        check(names.contains("toCIEXYZ"), "method toCIEXYZ");
        check(names.contains("fromCIEXYZ"), "method fromCIEXYZ");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
