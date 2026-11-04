/*
 * @test
 * @summary com.sun.corba.se.pept.protocol.MessageMediator conformance: type shape via reflection (headless-safe).
 * @run main MessageMediatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MessageMediatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.protocol.MessageMediator", false, MessageMediatorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("finishSendingRequest"), "method finishSendingRequest");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getContactInfo"), "method getContactInfo");
        check(mn.contains("getInputObject"), "method getInputObject");
        check(mn.contains("getOutputObject"), "method getOutputObject");
        check(mn.contains("initializeMessage"), "method initializeMessage");
        check(mn.contains("setInputObject"), "method setInputObject");
        check(mn.contains("setOutputObject"), "method setOutputObject");
        check(mn.contains("waitForResponse"), "method waitForResponse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
