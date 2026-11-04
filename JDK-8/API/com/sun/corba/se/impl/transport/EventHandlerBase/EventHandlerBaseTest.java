/*
 * @test
 * @summary com.sun.corba.se.impl.transport.EventHandlerBase conformance: type shape via reflection (headless-safe).
 * @run main EventHandlerBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EventHandlerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.EventHandlerBase", false, EventHandlerBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.EventHandler"), "interface com.sun.corba.se.pept.transport.EventHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
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
