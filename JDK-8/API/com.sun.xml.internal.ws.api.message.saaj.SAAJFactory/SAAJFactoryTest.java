/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.saaj.SAAJFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SAAJFactoryTest
 */

import java.lang.reflect.Modifier;


public class SAAJFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.saaj.SAAJFactory", false, SAAJFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.saaj.SAAJFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addAttachmentsToSOAPMessage"), "method addAttachmentsToSOAPMessage");
        check(mn.contains("create"), "method create");
        check(mn.contains("createMessage"), "method createMessage");
        check(mn.contains("createMessageFactory"), "method createMessageFactory");
        check(mn.contains("createSOAPFactory"), "method createSOAPFactory");
        check(mn.contains("getMessageFactory"), "method getMessageFactory");
        check(mn.contains("getSOAPFactory"), "method getSOAPFactory");
        check(mn.contains("read"), "method read");
        check(mn.contains("readAsSAAJ"), "method readAsSAAJ");
        check(mn.contains("readAsSOAPMessage"), "method readAsSOAPMessage");
        check(mn.contains("readAsSOAPMessageSax2Dom"), "method readAsSOAPMessageSax2Dom");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
