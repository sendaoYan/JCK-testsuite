/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage conformance: type shape via reflection (headless-safe).
 * @run main RequestMessageTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage", false, RequestMessageTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.protocol.giopmsgheaders.Message"), "interface com.sun.corba.se.impl.protocol.giopmsgheaders.Message");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("getObjectKey"), "method getObjectKey");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getPrincipal"), "method getPrincipal");
        check(mn.contains("getRequestId"), "method getRequestId");
        check(mn.contains("getReserved"), "method getReserved");
        check(mn.contains("getServiceContexts"), "method getServiceContexts");
        check(mn.contains("isResponseExpected"), "method isResponseExpected");
        check(mn.contains("setThreadPoolToUse"), "method setThreadPoolToUse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
