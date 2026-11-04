/*
 * @test
 * @summary javax.xml.soap.AttachmentPart conformance: abstract class hierarchy and methods.
 * @run main AttachmentPartTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttachmentPartTest {
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
        Class<?> c = Class.forName("javax.xml.soap.AttachmentPart");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSize"), "has method getSize");
        check(names.contains("clearContent"), "has method clearContent");
        check(names.contains("getContent"), "has method getContent");
        check(names.contains("getRawContent"), "has method getRawContent");
        check(names.contains("getRawContentBytes"), "has method getRawContentBytes");
        check(names.contains("getBase64Content"), "has method getBase64Content");
        check(names.contains("setContent"), "has method setContent");
        check(names.contains("setRawContent"), "has method setRawContent");
        check(names.contains("setRawContentBytes"), "has method setRawContentBytes");
        check(names.contains("setBase64Content"), "has method setBase64Content");
        check(names.contains("getDataHandler"), "has method getDataHandler");
        check(names.contains("setDataHandler"), "has method setDataHandler");
        check(names.contains("getContentId"), "has method getContentId");
        check(names.contains("getContentLocation"), "has method getContentLocation");
        check(names.contains("getContentType"), "has method getContentType");
        check(names.contains("setContentId"), "has method setContentId");
        check(names.contains("setContentLocation"), "has method setContentLocation");
        check(names.contains("setContentType"), "has method setContentType");
        check(names.contains("removeMimeHeader"), "has method removeMimeHeader");
        check(names.contains("removeAllMimeHeaders"), "has method removeAllMimeHeaders");
        check(names.contains("getMimeHeader"), "has method getMimeHeader");
        check(names.contains("setMimeHeader"), "has method setMimeHeader");
        check(names.contains("addMimeHeader"), "has method addMimeHeader");
        check(names.contains("getAllMimeHeaders"), "has method getAllMimeHeaders");
        check(names.contains("getMatchingMimeHeaders"), "has method getMatchingMimeHeaders");
        check(names.contains("getNonMatchingMimeHeaders"), "has method getNonMatchingMimeHeaders");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
