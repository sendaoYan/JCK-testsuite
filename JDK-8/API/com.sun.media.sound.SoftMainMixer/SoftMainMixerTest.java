/*
 * @test
 * @summary com.sun.media.sound.SoftMainMixer conformance: type shape via reflection (headless-safe).
 * @run main SoftMainMixerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftMainMixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftMainMixer", false, SoftMainMixerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftMainMixer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("activity"), "method activity");
        check(names.contains("stopMixer"), "method stopMixer");
        check(names.contains("registerMixer"), "method registerMixer");
        check(names.contains("getInputStream"), "method getInputStream");
        check(names.contains("reset"), "method reset");
        check(names.contains("setVolume"), "method setVolume");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
