/*
 * @test
 * @summary com.sun.media.sound.SoftMixingDataLine conformance: type shape via reflection (headless-safe).
 * @run main SoftMixingDataLineTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftMixingDataLineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftMixingDataLine", false, SoftMixingDataLineTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftMixingDataLine"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.sound.sampled.DataLine"), "implements javax.sound.sampled.DataLine");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("processControlLogic"), "method processControlLogic");
        check(names.contains("processAudioLogic"), "method processAudioLogic");
        check(names.contains("addLineListener"), "method addLineListener");
        check(names.contains("removeLineListener"), "method removeLineListener");
        check(names.contains("getLineInfo"), "method getLineInfo");
        check(names.contains("getControl"), "method getControl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
