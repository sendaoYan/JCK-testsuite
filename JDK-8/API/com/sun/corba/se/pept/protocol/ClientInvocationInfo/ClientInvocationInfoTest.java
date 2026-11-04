/*
 * @test
 * @summary com.sun.corba.se.pept.protocol.ClientInvocationInfo conformance: type shape via reflection (headless-safe).
 * @run main ClientInvocationInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ClientInvocationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.protocol.ClientInvocationInfo", false, ClientInvocationInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("decrementEntryCount"), "method decrementEntryCount");
        check(mn.contains("getClientRequestDispatcher"), "method getClientRequestDispatcher");
        check(mn.contains("getContactInfoListIterator"), "method getContactInfoListIterator");
        check(mn.contains("getEntryCount"), "method getEntryCount");
        check(mn.contains("getMessageMediator"), "method getMessageMediator");
        check(mn.contains("incrementEntryCount"), "method incrementEntryCount");
        check(mn.contains("isRetryInvocation"), "method isRetryInvocation");
        check(mn.contains("setClientRequestDispatcher"), "method setClientRequestDispatcher");
        check(mn.contains("setContactInfoListIterator"), "method setContactInfoListIterator");
        check(mn.contains("setIsRetryInvocation"), "method setIsRetryInvocation");
        check(mn.contains("setMessageMediator"), "method setMessageMediator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
