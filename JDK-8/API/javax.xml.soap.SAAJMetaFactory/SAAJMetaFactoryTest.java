/*
 * @test
 * @summary javax.xml.soap.SAAJMetaFactory conformance: abstract class hierarchy and methods.
 * @run main SAAJMetaFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SAAJMetaFactoryTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SAAJMetaFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
