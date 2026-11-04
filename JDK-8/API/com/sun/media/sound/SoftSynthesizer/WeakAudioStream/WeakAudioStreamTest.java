/*
 * @test
 * @summary com.sun.media.sound.SoftSynthesizer.WeakAudioStream conformance: type shape via reflection (headless-safe).
 * @run main WeakAudioStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WeakAudioStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftSynthesizer$WeakAudioStream", false, WeakAudioStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftSynthesizer$WeakAudioStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setInputStream"), "method setInputStream");
        check(names.contains("available"), "method available");
        check(names.contains("read"), "method read");
        check(names.contains("getAudioInputStream"), "method getAudioInputStream");
        check(names.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
