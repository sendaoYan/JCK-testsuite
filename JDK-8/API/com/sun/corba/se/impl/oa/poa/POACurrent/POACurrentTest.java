/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POACurrent conformance: type shape via reflection (headless-safe).
 * @run main POACurrentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POACurrentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POACurrent", false, POACurrentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.Current"), "interface org.omg.PortableServer.Current");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
        check(mn.contains("getOA"), "method getOA");
        check(mn.contains("getObjectId"), "method getObjectId");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("get_POA"), "method get_POA");
        check(mn.contains("get_object_id"), "method get_object_id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
