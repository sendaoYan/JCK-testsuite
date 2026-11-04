/*
 * @test
 * @summary javax.xml.bind.attachment.AttachmentUnmarshaller conformance: abstract class hierarchy and methods.
 * @run main AttachmentUnmarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttachmentUnmarshallerTest {
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
        Class<?> c = Class.forName("javax.xml.bind.attachment.AttachmentUnmarshaller");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttachmentAsDataHandler"), "has method getAttachmentAsDataHandler");
        check(names.contains("getAttachmentAsByteArray"), "has method getAttachmentAsByteArray");
        check(names.contains("isXOPPackage"), "has method isXOPPackage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
