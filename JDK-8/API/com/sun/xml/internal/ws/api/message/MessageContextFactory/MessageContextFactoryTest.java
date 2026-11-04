/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.MessageContextFactory conformance: reflection (kind, superclass, interfaces, methods).
 * @run main MessageContextFactoryTest
 */

import java.lang.reflect.Modifier;


public class MessageContextFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.MessageContextFactory", false, MessageContextFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.MessageContextFactory"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.MessageContextFactory"), "superclass com.oracle.webservices.internal.api.message.MessageContextFactory");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createContext"), "method createContext");
        check(mn.contains("doCreate"), "method doCreate");
        check(mn.contains("newFactory"), "method newFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
