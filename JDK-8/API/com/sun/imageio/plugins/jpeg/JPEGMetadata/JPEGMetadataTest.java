/*
 * @test
 * @summary com.sun.imageio.plugins.jpeg.JPEGMetadata conformance: type shape via reflection (headless-safe).
 * @run main JPEGMetadataTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JPEGMetadataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.imageio.plugins.jpeg.JPEGMetadata", false, JPEGMetadataTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.getName().equals("com.sun.imageio.plugins.jpeg.JPEGMetadata"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.metadata.IIOMetadata"), "superclass");
        check(hasInterface(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("clone"), "method clone");
        check(names.contains("getAsTree"), "method getAsTree");
        check(names.contains("getStandardChromaNode"), "method getStandardChromaNode");
        check(names.contains("getStandardCompressionNode"), "method getStandardCompressionNode");
        check(names.contains("getStandardDimensionNode"), "method getStandardDimensionNode");
        check(names.contains("getStandardTextNode"), "method getStandardTextNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
