/*
 * @test
 * @summary javax.xml.bind.Marshaller conformance: interface hierarchy and methods.
 * @run main MarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class MarshallerTest {
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
        Class<?> c = Class.forName("javax.xml.bind.Marshaller");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has method marshal");
        check(names.contains("getNode"), "has method getNode");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentMarshaller"), "has method setAttachmentMarshaller");
        check(names.contains("getAttachmentMarshaller"), "has method getAttachmentMarshaller");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
