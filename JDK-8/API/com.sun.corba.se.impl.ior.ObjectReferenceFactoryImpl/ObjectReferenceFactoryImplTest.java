/*
 * @test
 * @summary com.sun.corba.se.impl.ior.ObjectReferenceFactoryImpl conformance: type shape via reflection (headless-safe).
 * @run main ObjectReferenceFactoryImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectReferenceFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.ObjectReferenceFactoryImpl", false, ObjectReferenceFactoryImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.ior.ObjectReferenceProducerBase"), "superclass com.sun.corba.se.impl.ior.ObjectReferenceProducerBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.CORBA.portable.StreamableValue"), "interface org.omg.CORBA.portable.StreamableValue");
        check(in.contains("org.omg.PortableInterceptor.ObjectReferenceFactory"), "interface org.omg.PortableInterceptor.ObjectReferenceFactory");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("_read"), "method _read");
        check(mn.contains("_truncatable_ids"), "method _truncatable_ids");
        check(mn.contains("_type"), "method _type");
        check(mn.contains("_write"), "method _write");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getIORFactory"), "method getIORFactory");
        check(mn.contains("getIORTemplateList"), "method getIORTemplateList");
        check(mn.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
