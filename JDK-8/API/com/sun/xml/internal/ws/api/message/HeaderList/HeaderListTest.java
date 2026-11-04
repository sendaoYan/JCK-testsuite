/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.HeaderList conformance: reflection (kind, superclass, interfaces, methods).
 * @run main HeaderListTest
 */

import java.lang.reflect.Modifier;


public class HeaderListTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.HeaderList", false, HeaderListTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.HeaderList"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.util.ArrayList"), "superclass java.util.ArrayList");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.message.MessageHeaders"), "implements com.sun.xml.internal.ws.api.message.MessageHeaders");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("addAll"), "method addAll");
        check(mn.contains("addInternal"), "method addInternal");
        check(mn.contains("addOrReplace"), "method addOrReplace");
        check(mn.contains("asList"), "method asList");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("fillRequestAddressingHeaders"), "method fillRequestAddressingHeaders");
        check(mn.contains("get"), "method get");
        check(mn.contains("getAction"), "method getAction");
        check(mn.contains("getFaultTo"), "method getFaultTo");
        check(mn.contains("getHeaders"), "method getHeaders");
        check(mn.contains("getMessageID"), "method getMessageID");
        check(mn.contains("getNotUnderstoodHeaders"), "method getNotUnderstoodHeaders");
        check(mn.contains("getRelatesTo"), "method getRelatesTo");
        check(mn.contains("getReplyTo"), "method getReplyTo");
        check(mn.contains("getTo"), "method getTo");
        check(mn.contains("getUnderstoodHeaders"), "method getUnderstoodHeaders");
        check(mn.contains("hasHeaders"), "method hasHeaders");
        check(mn.contains("isUnderstood"), "method isUnderstood");
        check(mn.contains("readResponseAddressingHeaders"), "method readResponseAddressingHeaders");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("removeInternal"), "method removeInternal");
        check(mn.contains("replace"), "method replace");
        check(mn.contains("setSoapVersion"), "method setSoapVersion");
        check(mn.contains("size"), "method size");
        check(mn.contains("understood"), "method understood");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
