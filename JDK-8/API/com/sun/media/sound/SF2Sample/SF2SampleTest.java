/*
 * @test
 * @summary com.sun.media.sound.SF2Sample conformance: type shape via reflection (headless-safe).
 * @run main SF2SampleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SF2SampleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Sample", false, SF2SampleTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SF2Sample"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getData"), "method getData");
        check(names.contains("getDataBuffer"), "method getDataBuffer");
        check(names.contains("getData24Buffer"), "method getData24Buffer");
        check(names.contains("getFormat"), "method getFormat");
        check(names.contains("setData"), "method setData");
        check(names.contains("setData24"), "method setData24");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
