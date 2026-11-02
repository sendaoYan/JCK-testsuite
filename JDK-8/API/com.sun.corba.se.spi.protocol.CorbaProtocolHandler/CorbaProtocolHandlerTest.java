/*
 * @test
 * @summary com.sun.corba.se.spi.protocol.CorbaProtocolHandler conformance: type shape via reflection (headless-safe).
 * @run main CorbaProtocolHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaProtocolHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.protocol.CorbaProtocolHandler", false, CorbaProtocolHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.protocol.ProtocolHandler"), "interface com.sun.corba.se.pept.protocol.ProtocolHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("createLocationForward"), "method createLocationForward");
        check(mn.contains("createResponse"), "method createResponse");
        check(mn.contains("createSystemExceptionResponse"), "method createSystemExceptionResponse");
        check(mn.contains("createUnknownExceptionResponse"), "method createUnknownExceptionResponse");
        check(mn.contains("createUserExceptionResponse"), "method createUserExceptionResponse");
        check(mn.contains("handleRequest"), "method handleRequest");
        check(mn.contains("handleThrowableDuringServerDispatch"), "method handleThrowableDuringServerDispatch");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
