/*
 * @test
 * @summary com.sun.xml.internal.ws.message.AbstractMessageImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main AbstractMessageImplTest
 */

import java.lang.reflect.Modifier;


public class AbstractMessageImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.AbstractMessageImpl", false, AbstractMessageImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.AbstractMessageImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.message.Message"), "superclass com.sun.xml.internal.ws.api.message.Message");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("readAsSOAPMessage"), "method readAsSOAPMessage");
        check(mn.contains("readEnvelopeAsSource"), "method readEnvelopeAsSource");
        check(mn.contains("readPayloadAsJAXB"), "method readPayloadAsJAXB");
        check(mn.contains("toSAAJ"), "method toSAAJ");
        check(mn.contains("writePayloadTo"), "method writePayloadTo");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("DEFAULT_TAGS"), "field DEFAULT_TAGS");
        check(fn.contains("EMPTY_ATTS"), "field EMPTY_ATTS");
        check(fn.contains("NULL_LOCATOR"), "field NULL_LOCATOR");
        check(fn.contains("bodyTag"), "field bodyTag");
        check(fn.contains("envelopeTag"), "field envelopeTag");
        check(fn.contains("headerTag"), "field headerTag");
        check(fn.contains("soapVersion"), "field soapVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
