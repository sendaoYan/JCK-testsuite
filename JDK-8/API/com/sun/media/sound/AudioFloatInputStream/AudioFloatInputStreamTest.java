/*
 * @test
 * @summary com.sun.media.sound.AudioFloatInputStream conformance: type shape via reflection (headless-safe).
 * @run main AudioFloatInputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AudioFloatInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.AudioFloatInputStream", false, AudioFloatInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.AudioFloatInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getInputStream"), "method getInputStream");
        check(names.contains("getFormat"), "method getFormat");
        check(names.contains("getFrameLength"), "method getFrameLength");
        check(names.contains("read"), "method read");
        check(names.contains("skip"), "method skip");
        check(names.contains("available"), "method available");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
