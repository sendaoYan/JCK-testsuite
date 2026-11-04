/*
 * @test
 * @summary com.sun.corba.se.pept.transport.Connection conformance: type shape via reflection (headless-safe).
 * @run main ConnectionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.Connection", false, ConnectionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 20, "public declared method count");
        check(mn.contains("close"), "method close");
        check(mn.contains("getAcceptor"), "method getAcceptor");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getContactInfo"), "method getContactInfo");
        check(mn.contains("getEventHandler"), "method getEventHandler");
        check(mn.contains("getTimeStamp"), "method getTimeStamp");
        check(mn.contains("isBusy"), "method isBusy");
        check(mn.contains("isServer"), "method isServer");
        check(mn.contains("read"), "method read");
        check(mn.contains("registerWaiter"), "method registerWaiter");
        check(mn.contains("sendWithoutLock"), "method sendWithoutLock");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("setState"), "method setState");
        check(mn.contains("setTimeStamp"), "method setTimeStamp");
        check(mn.contains("shouldRegisterReadEvent"), "method shouldRegisterReadEvent");
        check(mn.contains("shouldRegisterServerReadEvent"), "method shouldRegisterServerReadEvent");
        check(mn.contains("unregisterWaiter"), "method unregisterWaiter");
        check(mn.contains("waitForResponse"), "method waitForResponse");
        check(mn.contains("writeLock"), "method writeLock");
        check(mn.contains("writeUnlock"), "method writeUnlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
