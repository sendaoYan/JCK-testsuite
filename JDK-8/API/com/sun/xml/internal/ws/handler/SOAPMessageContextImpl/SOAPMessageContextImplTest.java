/*
 * @test
 * @summary com.sun.xml.internal.ws.handler.SOAPMessageContextImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPMessageContextImplTest
 */

import java.lang.reflect.Modifier;


public class SOAPMessageContextImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.handler.SOAPMessageContextImpl", false, SOAPMessageContextImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.handler.SOAPMessageContextImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.handler.MessageUpdatableContext"), "superclass com.sun.xml.internal.ws.handler.MessageUpdatableContext");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.soap.SOAPMessageContext"), "implements javax.xml.ws.handler.soap.SOAPMessageContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getHeaders"), "method getHeaders");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getRoles"), "method getRoles");
        check(mn.contains("setMessage"), "method setMessage");
        check(mn.contains("updateMessage"), "method updateMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
