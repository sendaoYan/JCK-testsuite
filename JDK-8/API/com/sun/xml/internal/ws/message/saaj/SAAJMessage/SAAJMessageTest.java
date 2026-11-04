/*
 * @test
 * @summary com.sun.xml.internal.ws.message.saaj.SAAJMessage conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SAAJMessageTest
 */

import java.lang.reflect.Modifier;


public class SAAJMessageTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.message.saaj.SAAJMessage", false, SAAJMessageTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.message.saaj.SAAJMessage"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.message.Message"), "superclass com.sun.xml.internal.ws.api.message.Message");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("access"), "method access");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("createXMLStreamReader"), "method createXMLStreamReader");
        check(mn.contains("getAttachments"), "method getAttachments");
        check(mn.contains("getHeaders"), "method getHeaders");
        check(mn.contains("getPayloadLocalPart"), "method getPayloadLocalPart");
        check(mn.contains("getPayloadNamespaceURI"), "method getPayloadNamespaceURI");
        check(mn.contains("getSOAPVersion"), "method getSOAPVersion");
        check(mn.contains("getXMLStreamReader"), "method getXMLStreamReader");
        check(mn.contains("hasAttachments"), "method hasAttachments");
        check(mn.contains("hasHeaders"), "method hasHeaders");
        check(mn.contains("hasPayload"), "method hasPayload");
        check(mn.contains("readAsSOAPMessage"), "method readAsSOAPMessage");
        check(mn.contains("readEnvelopeAsSource"), "method readEnvelopeAsSource");
        check(mn.contains("readPayload"), "method readPayload");
        check(mn.contains("readPayloadAsJAXB"), "method readPayloadAsJAXB");
        check(mn.contains("readPayloadAsSource"), "method readPayloadAsSource");
        check(mn.contains("soapBodyFirstChildReader"), "method soapBodyFirstChildReader");
        check(mn.contains("writePayloadTo"), "method writePayloadTo");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
