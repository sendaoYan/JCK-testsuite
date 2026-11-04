/*
 * @test
 * @summary com.sun.media.sound.ModelAbstractChannelMixer conformance: type shape via reflection (headless-safe).
 * @run main ModelAbstractChannelMixerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelAbstractChannelMixerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelAbstractChannelMixer", false, ModelAbstractChannelMixerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelAbstractChannelMixer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "com.sun.media.sound.ModelChannelMixer"), "implements com.sun.media.sound.ModelChannelMixer");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("process"), "method process");
        check(names.contains("stop"), "method stop");
        check(names.contains("allNotesOff"), "method allNotesOff");
        check(names.contains("allSoundOff"), "method allSoundOff");
        check(names.contains("controlChange"), "method controlChange");
        check(names.contains("getChannelPressure"), "method getChannelPressure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
