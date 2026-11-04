/*
 * @test
 * @summary com.sun.media.sound.SoftMixingSourceDataLine conformance: type shape via reflection (headless-safe).
 * @run main SoftMixingSourceDataLineTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftMixingSourceDataLineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftMixingSourceDataLine", false, SoftMixingSourceDataLineTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftMixingSourceDataLine"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.media.sound.SoftMixingDataLine"), "superclass");
        check(hasInterface(c, "javax.sound.sampled.SourceDataLine"), "implements javax.sound.sampled.SourceDataLine");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("write"), "method write");
        check(names.contains("processControlLogic"), "method processControlLogic");
        check(names.contains("processAudioLogic"), "method processAudioLogic");
        check(names.contains("open"), "method open");
        check(names.contains("available"), "method available");
        check(names.contains("drain"), "method drain");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
