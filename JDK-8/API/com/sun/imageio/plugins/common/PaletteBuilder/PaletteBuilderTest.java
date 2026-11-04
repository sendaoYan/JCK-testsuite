/*
 * @test
 * @summary com.sun.imageio.plugins.common.PaletteBuilder conformance: type shape via reflection (headless-safe).
 * @run main PaletteBuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PaletteBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.common.PaletteBuilder", false, PaletteBuilderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.common.PaletteBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createIndexedImage"), "method createIndexedImage");
        check(names.contains("createIndexColorModel"), "method createIndexColorModel");
        check(names.contains("canCreatePalette"), "method canCreatePalette");
        check(names.contains("getIndexedImage"), "method getIndexedImage");
        check(names.contains("findColorIndex"), "method findColorIndex");
        check(names.contains("buildPalette"), "method buildPalette");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
