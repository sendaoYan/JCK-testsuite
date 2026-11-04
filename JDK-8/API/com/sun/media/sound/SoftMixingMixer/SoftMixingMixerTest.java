/*
 * @test
 * @summary com.sun.media.sound.SoftMixingMixer conformance: type shape via reflection (headless-safe).
 * @run main SoftMixingMixerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftMixingMixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftMixingMixer", false, SoftMixingMixerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftMixingMixer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.sound.sampled.Mixer"), "implements javax.sound.sampled.Mixer");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getLine"), "method getLine");
        check(names.contains("getMaxLines"), "method getMaxLines");
        check(names.contains("getMixerInfo"), "method getMixerInfo");
        check(names.contains("getSourceLineInfo"), "method getSourceLineInfo");
        check(names.contains("getSourceLines"), "method getSourceLines");
        check(names.contains("getTargetLineInfo"), "method getTargetLineInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
