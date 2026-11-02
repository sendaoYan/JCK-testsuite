/*
 * @test
 * @summary com.sun.media.sound.SF2Region conformance: type shape via reflection (headless-safe).
 * @run main SF2RegionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SF2RegionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Region", false, SF2RegionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SF2Region"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getGenerators"), "method getGenerators");
        check(names.contains("contains"), "method contains");
        check(names.contains("getDefaultValue"), "method getDefaultValue");
        check(names.contains("getShort"), "method getShort");
        check(names.contains("putShort"), "method putShort");
        check(names.contains("getBytes"), "method getBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
