/*
 * @test
 * @summary javax.xml.bind.Unmarshaller conformance: interface hierarchy and methods.
 * @run main UnmarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class UnmarshallerTest {
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
        Class<?> c = Class.forName("javax.xml.bind.Unmarshaller");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("unmarshal"), "has method unmarshal");
        check(names.contains("getUnmarshallerHandler"), "has method getUnmarshallerHandler");
        check(names.contains("setValidating"), "has method setValidating");
        check(names.contains("isValidating"), "has method isValidating");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentUnmarshaller"), "has method setAttachmentUnmarshaller");
        check(names.contains("getAttachmentUnmarshaller"), "has method getAttachmentUnmarshaller");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
