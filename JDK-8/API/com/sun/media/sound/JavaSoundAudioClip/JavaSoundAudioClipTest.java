/*
 * @test
 * @summary com.sun.media.sound.JavaSoundAudioClip conformance: type shape via reflection (headless-safe).
 * @run main JavaSoundAudioClipTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JavaSoundAudioClipTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.JavaSoundAudioClip", false, JavaSoundAudioClipTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.JavaSoundAudioClip"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.applet.AudioClip"), "implements java.applet.AudioClip");
        check(hasInterface(c, "javax.sound.midi.MetaEventListener"), "implements javax.sound.midi.MetaEventListener");
        check(hasInterface(c, "javax.sound.sampled.LineListener"), "implements javax.sound.sampled.LineListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("play"), "method play");
        check(names.contains("loop"), "method loop");
        check(names.contains("stop"), "method stop");
        check(names.contains("update"), "method update");
        check(names.contains("meta"), "method meta");
        check(names.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
