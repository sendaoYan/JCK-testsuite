/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.Message conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MessageTest
 */

import java.lang.reflect.Modifier;


public class MessageTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.Message", false, MessageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.Message"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSOAPMimeHeaders"), "method addSOAPMimeHeaders");
        check(mn.contains("assertOneWay"), "method assertOneWay");
        check(mn.contains("consume"), "method consume");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("generateMessageID"), "method generateMessageID");
        check(mn.contains("getAttachments"), "method getAttachments");
        check(mn.contains("getFirstDetailEntryName"), "method getFirstDetailEntryName");
        check(mn.contains("getHeaders"), "method getHeaders");
        check(mn.contains("getID"), "method getID");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getPayloadLocalPart"), "method getPayloadLocalPart");
        check(mn.contains("getPayloadNamespaceURI"), "method getPayloadNamespaceURI");
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("getTransportHeaders"), "method getTransportHeaders");
        check(mn.contains("hasAttachments"), "method hasAttachments");
        check(mn.contains("hasHeaders"), "method hasHeaders");
        check(mn.contains("hasPayload"), "method hasPayload");
        check(mn.contains("isFault"), "method isFault");
        check(mn.contains("isOneWay"), "method isOneWay");
        check(mn.contains("readAsSOAPMessage"), "method readAsSOAPMessage");
        check(mn.contains("readEnvelopeAsSource"), "method readEnvelopeAsSource");
        check(mn.contains("readPayload"), "method readPayload");
        check(mn.contains("readPayloadAsJAXB"), "method readPayloadAsJAXB");
        check(mn.contains("readPayloadAsSource"), "method readPayloadAsSource");
        check(mn.contains("setMessageMedadata"), "method setMessageMedadata");
        check(mn.contains("writePayloadTo"), "method writePayloadTo");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("attachmentSet"), "field attachmentSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
