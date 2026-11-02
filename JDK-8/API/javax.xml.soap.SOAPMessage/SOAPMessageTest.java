/*
 * @test
 * @summary javax.xml.soap.SOAPMessage conformance: abstract class hierarchy and methods.
 * @run main SOAPMessageTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPMessageTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPMessage");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setContentDescription"), "has method setContentDescription");
        check(names.contains("getContentDescription"), "has method getContentDescription");
        check(names.contains("getSOAPPart"), "has method getSOAPPart");
        check(names.contains("getSOAPBody"), "has method getSOAPBody");
        check(names.contains("getSOAPHeader"), "has method getSOAPHeader");
        check(names.contains("removeAllAttachments"), "has method removeAllAttachments");
        check(names.contains("countAttachments"), "has method countAttachments");
        check(names.contains("getAttachments"), "has method getAttachments");
        check(names.contains("removeAttachments"), "has method removeAttachments");
        check(names.contains("getAttachment"), "has method getAttachment");
        check(names.contains("addAttachmentPart"), "has method addAttachmentPart");
        check(names.contains("createAttachmentPart"), "has method createAttachmentPart");
        check(names.contains("getMimeHeaders"), "has method getMimeHeaders");
        check(names.contains("saveChanges"), "has method saveChanges");
        check(names.contains("saveRequired"), "has method saveRequired");
        check(names.contains("writeTo"), "has method writeTo");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
