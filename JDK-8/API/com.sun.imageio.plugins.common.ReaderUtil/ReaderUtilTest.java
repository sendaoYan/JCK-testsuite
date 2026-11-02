/*
 * @test
 * @summary com.sun.imageio.plugins.common.ReaderUtil conformance: type shape via reflection (headless-safe).
 * @run main ReaderUtilTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ReaderUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.common.ReaderUtil", false, ReaderUtilTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.common.ReaderUtil"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("computeUpdatedPixels"), "method computeUpdatedPixels");
        check(names.contains("readMultiByteInteger"), "method readMultiByteInteger");
        check(names.contains("staggeredReadByteStream"), "method staggeredReadByteStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
