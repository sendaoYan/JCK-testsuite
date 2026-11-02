/*
 * @test
 * @summary com.sun.media.sound.SoftMixingMainMixer conformance: type shape via reflection (headless-safe).
 * @run main SoftMixingMainMixerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftMixingMainMixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftMixingMainMixer", false, SoftMixingMainMixerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftMixingMainMixer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getInputStream"), "method getInputStream");
        check(names.contains("openLine"), "method openLine");
        check(names.contains("closeLine"), "method closeLine");
        check(names.contains("getOpenLines"), "method getOpenLines");
        check(names.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
