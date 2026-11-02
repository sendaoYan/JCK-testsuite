/*
 * @test
 * @summary com.sun.media.sound.SoftFilter conformance: type shape via reflection (headless-safe).
 * @run main SoftFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftFilter", false, SoftFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftFilter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setFrequency"), "method setFrequency");
        check(names.contains("setResonance"), "method setResonance");
        check(names.contains("reset"), "method reset");
        check(names.contains("setFilterType"), "method setFilterType");
        check(names.contains("processAudio"), "method processAudio");
        check(names.contains("filter4"), "method filter4");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
