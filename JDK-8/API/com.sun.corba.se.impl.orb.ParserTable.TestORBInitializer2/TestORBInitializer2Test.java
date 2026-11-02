/*
 * @test
 * @summary com.sun.corba.se.impl.orb.ParserTable.TestORBInitializer2 conformance: type shape via reflection (headless-safe).
 * @run main TestORBInitializer2Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TestORBInitializer2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orb.ParserTable$TestORBInitializer2", false, TestORBInitializer2Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableInterceptor.ORBInitializer"), "interface org.omg.PortableInterceptor.ORBInitializer");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("post_init"), "method post_init");
        check(mn.contains("pre_init"), "method pre_init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
