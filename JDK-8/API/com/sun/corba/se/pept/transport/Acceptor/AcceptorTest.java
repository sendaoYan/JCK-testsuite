/*
 * @test
 * @summary com.sun.corba.se.pept.transport.Acceptor conformance: type shape via reflection (headless-safe).
 * @run main AcceptorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AcceptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.Acceptor", false, AcceptorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("accept"), "method accept");
        check(mn.contains("close"), "method close");
        check(mn.contains("createInputObject"), "method createInputObject");
        check(mn.contains("createMessageMediator"), "method createMessageMediator");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("finishCreatingMessageMediator"), "method finishCreatingMessageMediator");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getConnectionCacheType"), "method getConnectionCacheType");
        check(mn.contains("getEventHandler"), "method getEventHandler");
        check(mn.contains("initialize"), "method initialize");
        check(mn.contains("initialized"), "method initialized");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("shouldRegisterAcceptEvent"), "method shouldRegisterAcceptEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
