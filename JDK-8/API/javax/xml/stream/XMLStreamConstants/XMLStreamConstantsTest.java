/*
 * @test
 * @summary javax.xml.stream.XMLStreamConstants conformance: interface hierarchy and methods.
 * @run main XMLStreamConstantsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLStreamConstantsTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamConstants");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
