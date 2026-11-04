/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.DelegateImpl conformance: type shape via reflection (headless-safe).
 * @run main DelegateImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DelegateImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.DelegateImpl", false, DelegateImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.portable.Delegate"), "interface org.omg.PortableServer.portable.Delegate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("default_POA"), "method default_POA");
        check(mn.contains("get_interface_def"), "method get_interface_def");
        check(mn.contains("is_a"), "method is_a");
        check(mn.contains("non_existent"), "method non_existent");
        check(mn.contains("object_id"), "method object_id");
        check(mn.contains("orb"), "method orb");
        check(mn.contains("poa"), "method poa");
        check(mn.contains("this_object"), "method this_object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
