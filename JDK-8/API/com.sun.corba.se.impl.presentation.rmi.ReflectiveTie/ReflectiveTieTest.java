/*
 * @test
 * @summary com.sun.corba.se.impl.presentation.rmi.ReflectiveTie conformance: type shape via reflection (headless-safe).
 * @run main ReflectiveTieTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ReflectiveTieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.presentation.rmi.ReflectiveTie", false, ReflectiveTieTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.PortableServer.Servant"), "superclass org.omg.PortableServer.Servant");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.rmi.CORBA.Tie"), "interface javax.rmi.CORBA.Tie");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("_all_interfaces"), "method _all_interfaces");
        check(mn.contains("_invoke"), "method _invoke");
        check(mn.contains("deactivate"), "method deactivate");
        check(mn.contains("getTarget"), "method getTarget");
        check(mn.contains("orb"), "method orb");
        check(mn.contains("setTarget"), "method setTarget");
        check(mn.contains("thisObject"), "method thisObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
