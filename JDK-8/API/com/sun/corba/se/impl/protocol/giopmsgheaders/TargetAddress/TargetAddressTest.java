/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.giopmsgheaders.TargetAddress conformance: type shape via reflection (headless-safe).
 * @run main TargetAddressTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TargetAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.giopmsgheaders.TargetAddress", false, TargetAddressTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.CORBA.portable.IDLEntity"), "interface org.omg.CORBA.portable.IDLEntity");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("_default"), "method _default");
        check(mn.contains("discriminator"), "method discriminator");
        check(mn.contains("ior"), "method ior");
        check(mn.contains("object_key"), "method object_key");
        check(mn.contains("profile"), "method profile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
