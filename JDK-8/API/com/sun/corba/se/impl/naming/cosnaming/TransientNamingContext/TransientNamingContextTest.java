/*
 * @test
 * @summary com.sun.corba.se.impl.naming.cosnaming.TransientNamingContext conformance: type shape via reflection (headless-safe).
 * @run main TransientNamingContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TransientNamingContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.cosnaming.TransientNamingContext", false, TransientNamingContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.naming.cosnaming.NamingContextImpl"), "superclass com.sun.corba.se.impl.naming.cosnaming.NamingContextImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore"), "interface com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("Bind"), "method Bind");
        check(mn.contains("Destroy"), "method Destroy");
        check(mn.contains("IsEmpty"), "method IsEmpty");
        check(mn.contains("List"), "method List");
        check(mn.contains("NewContext"), "method NewContext");
        check(mn.contains("Resolve"), "method Resolve");
        check(mn.contains("Unbind"), "method Unbind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
