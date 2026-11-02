/*
 * @test
 * @summary com.sun.media.sound.SoftEnvelopeGenerator conformance: type shape via reflection (headless-safe).
 * @run main SoftEnvelopeGeneratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftEnvelopeGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftEnvelopeGenerator", false, SoftEnvelopeGeneratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftEnvelopeGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "com.sun.media.sound.SoftProcess"), "implements com.sun.media.sound.SoftProcess");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("reset"), "method reset");
        check(names.contains("init"), "method init");
        check(names.contains("get"), "method get");
        check(names.contains("processControlLogic"), "method processControlLogic");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
