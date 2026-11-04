/*
 * @test
 * @summary com.sun.corba.se.spi.activation._InitialNameServiceImplBase conformance: type shape via reflection (headless-safe).
 * @run main _InitialNameServiceImplBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class _InitialNameServiceImplBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation._InitialNameServiceImplBase", false, _InitialNameServiceImplBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.activation.InitialNameService"), "interface com.sun.corba.se.spi.activation.InitialNameService");
        check(in.contains("org.omg.CORBA.portable.InvokeHandler"), "interface org.omg.CORBA.portable.InvokeHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 2, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
        check(mn.contains("_invoke"), "method _invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
