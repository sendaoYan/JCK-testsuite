/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage_1_0 conformance: type shape via reflection (headless-safe).
 * @run main RequestMessage_1_0Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestMessage_1_0Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage_1_0", false, RequestMessage_1_0Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_0"), "superclass com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_0");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage"), "interface com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("callback"), "method callback");
        check(mn.contains("getObjectKey"), "method getObjectKey");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getPrincipal"), "method getPrincipal");
        check(mn.contains("getRequestId"), "method getRequestId");
        check(mn.contains("getReserved"), "method getReserved");
        check(mn.contains("getServiceContexts"), "method getServiceContexts");
        check(mn.contains("isResponseExpected"), "method isResponseExpected");
        check(mn.contains("read"), "method read");
        check(mn.contains("setThreadPoolToUse"), "method setThreadPoolToUse");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
