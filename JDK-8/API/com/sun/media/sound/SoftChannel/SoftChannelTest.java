/*
 * @test
 * @summary com.sun.media.sound.SoftChannel conformance: type shape via reflection (headless-safe).
 * @run main SoftChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftChannel", false, SoftChannelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftChannel"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.sound.midi.MidiChannel"), "implements javax.sound.midi.MidiChannel");
        check(hasInterface(c, "com.sun.media.sound.ModelDirectedPlayer"), "implements com.sun.media.sound.ModelDirectedPlayer");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("noteOn"), "method noteOn");
        check(names.contains("noteOff"), "method noteOff");
        check(names.contains("play"), "method play");
        check(names.contains("setPolyPressure"), "method setPolyPressure");
        check(names.contains("getPolyPressure"), "method getPolyPressure");
        check(names.contains("setChannelPressure"), "method setChannelPressure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
