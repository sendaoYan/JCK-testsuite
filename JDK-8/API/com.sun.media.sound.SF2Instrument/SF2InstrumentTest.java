/*
 * @test
 * @summary com.sun.media.sound.SF2Instrument conformance: type shape via reflection (headless-safe).
 * @run main SF2InstrumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SF2InstrumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SF2Instrument", false, SF2InstrumentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SF2Instrument"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.media.sound.ModelInstrument"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("setName"), "method setName");
        check(names.contains("getPatch"), "method getPatch");
        check(names.contains("setPatch"), "method setPatch");
        check(names.contains("getData"), "method getData");
        check(names.contains("getGenre"), "method getGenre");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
