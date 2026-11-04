/*
 * @test
 * @summary com.sun.imageio.plugins.png.PNGImageWriter conformance: type shape via reflection (headless-safe).
 * @run main PNGImageWriterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PNGImageWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.png.PNGImageWriter", false, PNGImageWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.png.PNGImageWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.ImageWriter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("setOutput"), "method setOutput");
        check(names.contains("getDefaultWriteParam"), "method getDefaultWriteParam");
        check(names.contains("getDefaultStreamMetadata"), "method getDefaultStreamMetadata");
        check(names.contains("getDefaultImageMetadata"), "method getDefaultImageMetadata");
        check(names.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(names.contains("convertImageMetadata"), "method convertImageMetadata");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
