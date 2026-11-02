/*
 * @test
 * @summary javax.xml.bind.JAXBContext conformance: abstract class hierarchy and methods.
 * @run main JAXBContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class JAXBContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.JAXBContext");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("newInstance"), "has method newInstance");
        check(names.contains("createUnmarshaller"), "has method createUnmarshaller");
        check(names.contains("createMarshaller"), "has method createMarshaller");
        check(names.contains("createValidator"), "has method createValidator");
        check(names.contains("createBinder"), "has method createBinder");
        check(names.contains("createJAXBIntrospector"), "has method createJAXBIntrospector");
        check(names.contains("generateSchema"), "has method generateSchema");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
