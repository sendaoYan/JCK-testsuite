/*
 * @test
 * @summary com.sun.corba.se.pept.transport.ContactInfo conformance: type shape via reflection (headless-safe).
 * @run main ContactInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ContactInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.ContactInfo", false, ContactInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 14, "public declared method count");
        check(mn.contains("createConnection"), "method createConnection");
        check(mn.contains("createInputObject"), "method createInputObject");
        check(mn.contains("createMessageMediator"), "method createMessageMediator");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("finishCreatingMessageMediator"), "method finishCreatingMessageMediator");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getClientRequestDispatcher"), "method getClientRequestDispatcher");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getConnectionCacheType"), "method getConnectionCacheType");
        check(mn.contains("getContactInfoList"), "method getContactInfoList");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isConnectionBased"), "method isConnectionBased");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("shouldCacheConnection"), "method shouldCacheConnection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
