/*
 * @test
 * @summary com.sun.imageio.plugins.jpeg.JPEGImageReader conformance: type shape via reflection (headless-safe).
 * @run main JPEGImageReaderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JPEGImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.jpeg.JPEGImageReader", false, JPEGImageReaderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.jpeg.JPEGImageReader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.ImageReader"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("warningOccurred"), "method warningOccurred");
        check(names.contains("warningWithMessage"), "method warningWithMessage");
        check(names.contains("setInput"), "method setInput");
        check(names.contains("getNumImages"), "method getNumImages");
        check(names.contains("getWidth"), "method getWidth");
        check(names.contains("getHeight"), "method getHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
