/*
 * @test
 * @summary com.sun.corba.se.spi.orbutil.fsm.StateImpl conformance: type shape via reflection (headless-safe).
 * @run main StateImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StateImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orbutil.fsm.StateImpl", false, StateImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.orbutil.fsm.NameBase"), "superclass com.sun.corba.se.impl.orbutil.fsm.NameBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.orbutil.fsm.State"), "interface com.sun.corba.se.spi.orbutil.fsm.State");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("addGuardedAction"), "method addGuardedAction");
        check(mn.contains("getDefaultAction"), "method getDefaultAction");
        check(mn.contains("getDefaultNextState"), "method getDefaultNextState");
        check(mn.contains("getGuardedActions"), "method getGuardedActions");
        check(mn.contains("postAction"), "method postAction");
        check(mn.contains("preAction"), "method preAction");
        check(mn.contains("setDefaultAction"), "method setDefaultAction");
        check(mn.contains("setDefaultNextState"), "method setDefaultNextState");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
