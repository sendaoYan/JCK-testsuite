/*
 * @test
 * @summary com.sun.corba.se.spi.orb.ORBVersionFactory conformance: type shape via reflection (headless-safe).
 * @run main ORBVersionFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ORBVersionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orb.ORBVersionFactory", false, ORBVersionFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("create"), "method create");
        check(mn.contains("getFOREIGN"), "method getFOREIGN");
        check(mn.contains("getJDK1_3_1_01"), "method getJDK1_3_1_01");
        check(mn.contains("getNEW"), "method getNEW");
        check(mn.contains("getNEWER"), "method getNEWER");
        check(mn.contains("getOLD"), "method getOLD");
        check(mn.contains("getORBVersion"), "method getORBVersion");
        check(mn.contains("getPEORB"), "method getPEORB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
