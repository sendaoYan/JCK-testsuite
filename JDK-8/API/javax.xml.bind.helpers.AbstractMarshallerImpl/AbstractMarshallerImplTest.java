/*
 * @test
 * @summary javax.xml.bind.helpers.AbstractMarshallerImpl conformance: abstract class hierarchy and methods.
 * @run main AbstractMarshallerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AbstractMarshallerImplTest {
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
        Class<?> c = Class.forName("javax.xml.bind.helpers.AbstractMarshallerImpl");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.Marshaller"), "implements javax.xml.bind.Marshaller");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has method marshal");
        check(names.contains("getNode"), "has method getNode");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentMarshaller"), "has method setAttachmentMarshaller");
        check(names.contains("getAttachmentMarshaller"), "has method getAttachmentMarshaller");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
