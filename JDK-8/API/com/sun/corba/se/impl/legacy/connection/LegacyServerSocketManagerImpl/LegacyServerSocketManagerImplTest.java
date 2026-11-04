/*
 * @test
 * @summary com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main LegacyServerSocketManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LegacyServerSocketManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl", false, LegacyServerSocketManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager"), "interface com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("legacyGetEndpoint"), "method legacyGetEndpoint");
        check(mn.contains("legacyGetPersistentServerPort"), "method legacyGetPersistentServerPort");
        check(mn.contains("legacyGetTransientOrPersistentServerPort"), "method legacyGetTransientOrPersistentServerPort");
        check(mn.contains("legacyGetTransientServerPort"), "method legacyGetTransientServerPort");
        check(mn.contains("legacyIsLocalServerPort"), "method legacyIsLocalServerPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
