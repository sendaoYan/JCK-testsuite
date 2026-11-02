/*
 * @test
 * @summary com.sun.media.sound.ModelOscillatorStream conformance: type shape via reflection (headless-safe).
 * @run main ModelOscillatorStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelOscillatorStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.ModelOscillatorStream", false, ModelOscillatorStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.ModelOscillatorStream"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setPitch"), "method setPitch");
        check(names.contains("noteOn"), "method noteOn");
        check(names.contains("noteOff"), "method noteOff");
        check(names.contains("read"), "method read");
        check(names.contains("close"), "method close");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
