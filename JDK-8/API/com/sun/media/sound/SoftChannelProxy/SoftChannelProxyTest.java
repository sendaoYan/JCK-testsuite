/*
 * @test
 * @summary com.sun.media.sound.SoftChannelProxy conformance: type shape via reflection (headless-safe).
 * @run main SoftChannelProxyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftChannelProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftChannelProxy", false, SoftChannelProxyTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftChannelProxy"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.sound.midi.MidiChannel"), "implements javax.sound.midi.MidiChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getChannel"), "method getChannel");
        check(names.contains("setChannel"), "method setChannel");
        check(names.contains("allNotesOff"), "method allNotesOff");
        check(names.contains("allSoundOff"), "method allSoundOff");
        check(names.contains("controlChange"), "method controlChange");
        check(names.contains("getChannelPressure"), "method getChannelPressure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
