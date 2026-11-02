/*
 * @test
 * @summary com.oracle.webservices.internal.api.message.MessageContext conformance: type shape via reflection (headless-safe).
 * @run main MessageContextTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MessageContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.webservices.internal.api.message.MessageContext", false, MessageContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.oracle.webservices.internal.api.message.DistributedPropertySet"), "implements com.oracle.webservices.internal.api.message.DistributedPropertySet");
        check(hasMethod(c, "getAsSOAPMessage"), "declares getAsSOAPMessage");
        check(hasMethod(c, "getContentType"), "declares getContentType");
        check(hasMethod(c, "getSOAPMessage"), "declares getSOAPMessage");
        check(hasMethod(c, "writeTo"), "declares writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
