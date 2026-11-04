/*
 * @test
 * @summary com.sun.xml.internal.ws.handler.MessageUpdatableContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MessageUpdatableContextTest
 */

import java.lang.reflect.Modifier;


public class MessageUpdatableContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.handler.MessageUpdatableContext", false, MessageUpdatableContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.handler.MessageUpdatableContext"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.handler.MessageContext"), "implements javax.xml.ws.handler.MessageContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "method clear");
        check(mn.contains("containsKey"), "method containsKey");
        check(mn.contains("containsValue"), "method containsValue");
        check(mn.contains("entrySet"), "method entrySet");
        check(mn.contains("get"), "method get");
        check(mn.contains("getScope"), "method getScope");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("keySet"), "method keySet");
        check(mn.contains("put"), "method put");
        check(mn.contains("putAll"), "method putAll");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("setScope"), "method setScope");
        check(mn.contains("size"), "method size");
        check(mn.contains("updatePacket"), "method updatePacket");
        check(mn.contains("values"), "method values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
