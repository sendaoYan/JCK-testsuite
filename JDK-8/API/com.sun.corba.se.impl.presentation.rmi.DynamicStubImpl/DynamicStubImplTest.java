/*
 * @test
 * @summary com.sun.corba.se.impl.presentation.rmi.DynamicStubImpl conformance: type shape via reflection (headless-safe).
 * @run main DynamicStubImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DynamicStubImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.presentation.rmi.DynamicStubImpl", false, DynamicStubImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA_2_3.portable.ObjectImpl"), "superclass org.omg.CORBA_2_3.portable.ObjectImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.presentation.rmi.DynamicStub"), "interface com.sun.corba.se.spi.presentation.rmi.DynamicStub");
        check(in.contains("java.io.Serializable"), "interface java.io.Serializable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
        check(mn.contains("connect"), "method connect");
        check(mn.contains("getDelegate"), "method getDelegate");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("getSelf"), "method getSelf");
        check(mn.contains("getTypeIds"), "method getTypeIds");
        check(mn.contains("isLocal"), "method isLocal");
        check(mn.contains("readResolve"), "method readResolve");
        check(mn.contains("request"), "method request");
        check(mn.contains("setDelegate"), "method setDelegate");
        check(mn.contains("setSelf"), "method setSelf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
