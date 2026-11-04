/*
 * @test
 * @summary com.sun.media.sound.EmergencySoundbank conformance: type shape via reflection (headless-safe).
 * @run main EmergencySoundbankTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EmergencySoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.EmergencySoundbank", false, EmergencySoundbankTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.EmergencySoundbank"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createSoundbank"), "method createSoundbank");
        check(names.contains("new_bell"), "method new_bell");
        check(names.contains("new_guitar1"), "method new_guitar1");
        check(names.contains("new_guitar_dist"), "method new_guitar_dist");
        check(names.contains("new_guitar_pick"), "method new_guitar_pick");
        check(names.contains("new_gpiano"), "method new_gpiano");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
