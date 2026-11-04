/*
 * @test
 * @summary javax.xml.soap.SOAPPart conformance: abstract class hierarchy and methods.
 * @run main SOAPPartTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class SOAPPartTest {
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
        Class<?> c = Class.forName("javax.xml.soap.SOAPPart");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.w3c.dom.Document"), "implements org.w3c.dom.Document");
        check(has(ifs, "javax.xml.soap.Node"), "implements javax.xml.soap.Node");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getEnvelope"), "has method getEnvelope");
        check(names.contains("getContentId"), "has method getContentId");
        check(names.contains("getContentLocation"), "has method getContentLocation");
        check(names.contains("setContentId"), "has method setContentId");
        check(names.contains("setContentLocation"), "has method setContentLocation");
        check(names.contains("removeMimeHeader"), "has method removeMimeHeader");
        check(names.contains("removeAllMimeHeaders"), "has method removeAllMimeHeaders");
        check(names.contains("getMimeHeader"), "has method getMimeHeader");
        check(names.contains("setMimeHeader"), "has method setMimeHeader");
        check(names.contains("addMimeHeader"), "has method addMimeHeader");
        check(names.contains("getAllMimeHeaders"), "has method getAllMimeHeaders");
        check(names.contains("getMatchingMimeHeaders"), "has method getMatchingMimeHeaders");
        check(names.contains("getNonMatchingMimeHeaders"), "has method getNonMatchingMimeHeaders");
        check(names.contains("setContent"), "has method setContent");
        check(names.contains("getContent"), "has method getContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
