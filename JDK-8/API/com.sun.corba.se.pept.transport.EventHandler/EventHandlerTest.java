/*
 * @test
 * @summary com.sun.corba.se.pept.transport.EventHandler conformance: type shape via reflection (headless-safe).
 * @run main EventHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EventHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.EventHandler", false, EventHandlerTest.class.getClassLoader());
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
        check(mn.contains("getAcceptor"), "method getAcceptor");
        check(mn.contains("getChannel"), "method getChannel");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getInterestOps"), "method getInterestOps");
        check(mn.contains("getSelectionKey"), "method getSelectionKey");
        check(mn.contains("getWork"), "method getWork");
        check(mn.contains("handleEvent"), "method handleEvent");
        check(mn.contains("setSelectionKey"), "method setSelectionKey");
        check(mn.contains("setUseSelectThreadToWait"), "method setUseSelectThreadToWait");
        check(mn.contains("setUseWorkerThreadForEvent"), "method setUseWorkerThreadForEvent");
        check(mn.contains("setWork"), "method setWork");
        check(mn.contains("shouldUseSelectThreadToWait"), "method shouldUseSelectThreadToWait");
        check(mn.contains("shouldUseWorkerThreadForEvent"), "method shouldUseWorkerThreadForEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
