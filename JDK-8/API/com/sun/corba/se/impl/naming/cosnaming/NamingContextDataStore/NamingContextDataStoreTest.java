/*
 * @test
 * @summary com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore conformance: type shape via reflection (headless-safe).
 * @run main NamingContextDataStoreTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NamingContextDataStoreTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore", false, NamingContextDataStoreTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("Bind"), "method Bind");
        check(mn.contains("Destroy"), "method Destroy");
        check(mn.contains("IsEmpty"), "method IsEmpty");
        check(mn.contains("List"), "method List");
        check(mn.contains("NewContext"), "method NewContext");
        check(mn.contains("Resolve"), "method Resolve");
        check(mn.contains("Unbind"), "method Unbind");
        check(mn.contains("getNSPOA"), "method getNSPOA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
