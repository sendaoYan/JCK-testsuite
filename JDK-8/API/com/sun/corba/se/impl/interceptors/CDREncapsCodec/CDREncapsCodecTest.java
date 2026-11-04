/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.CDREncapsCodec conformance: type shape via reflection (headless-safe).
 * @run main CDREncapsCodecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CDREncapsCodecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.CDREncapsCodec", false, CDREncapsCodecTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.IOP.Codec"), "interface org.omg.IOP.Codec");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("decode"), "method decode");
        check(mn.contains("decode_value"), "method decode_value");
        check(mn.contains("encode"), "method encode");
        check(mn.contains("encode_value"), "method encode_value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
