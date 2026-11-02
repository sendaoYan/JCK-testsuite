/*
 * @test
 * @summary com.sun.corba.se.impl.orb.ParserTable.TestAcceptor2 conformance: type shape via reflection (headless-safe).
 * @run main TestAcceptor2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TestAcceptor2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orb.ParserTable$TestAcceptor2", false, TestAcceptor2Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.Acceptor"), "interface com.sun.corba.se.pept.transport.Acceptor");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 19, "public declared method count");
        check(mn.contains("accept"), "method accept");
        check(mn.contains("close"), "method close");
        check(mn.contains("createInputObject"), "method createInputObject");
        check(mn.contains("createMessageMediator"), "method createMessageMediator");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("finishCreatingMessageMediator"), "method finishCreatingMessageMediator");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getConnectionCacheType"), "method getConnectionCacheType");
        check(mn.contains("getEventHandler"), "method getEventHandler");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("initialize"), "method initialize");
        check(mn.contains("initialized"), "method initialized");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("setUseSelectThreadForConnections"), "method setUseSelectThreadForConnections");
        check(mn.contains("setUseWorkerThreadForConnections"), "method setUseWorkerThreadForConnections");
        check(mn.contains("shouldRegisterAcceptEvent"), "method shouldRegisterAcceptEvent");
        check(mn.contains("shouldUseSelectThreadForConnections"), "method shouldUseSelectThreadForConnections");
        check(mn.contains("shouldUseWorkerThreadForConnections"), "method shouldUseWorkerThreadForConnections");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
