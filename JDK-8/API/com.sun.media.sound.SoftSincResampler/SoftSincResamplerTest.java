/*
 * @test
 * @summary com.sun.media.sound.SoftSincResampler conformance: type shape via reflection (headless-safe).
 * @run main SoftSincResamplerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class SoftSincResamplerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.media.sound.SoftSincResampler", false, SoftSincResamplerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.media.sound.SoftSincResampler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.media.sound.SoftAbstractResampler"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("sinc"), "method sinc");
        check(names.contains("wHanning"), "method wHanning");
        check(names.contains("sincTable"), "method sincTable");
        check(names.contains("getPadding"), "method getPadding");
        check(names.contains("interpolate"), "method interpolate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
