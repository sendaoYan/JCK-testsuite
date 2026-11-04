/*
 * @test
 * @summary com.sun.media.sound.SimpleSoundbank conformance: type shape via reflection (headless-safe).
 * @run main SimpleSoundbankTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SimpleSoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SimpleSoundbank", false, SimpleSoundbankTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SimpleSoundbank"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.sound.midi.Soundbank"), "implements javax.sound.midi.Soundbank");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getVersion"), "method getVersion");
        check(names.contains("getVendor"), "method getVendor");
        check(names.contains("getDescription"), "method getDescription");
        check(names.contains("setDescription"), "method setDescription");
        check(names.contains("setName"), "method setName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
