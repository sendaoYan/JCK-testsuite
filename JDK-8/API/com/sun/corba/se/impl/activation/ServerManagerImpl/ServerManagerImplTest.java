/*
 * @test
 * @summary com.sun.corba.se.impl.activation.ServerManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main ServerManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ServerManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.activation.ServerManagerImpl", false, ServerManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.activation._ServerManagerImplBase"), "superclass com.sun.corba.se.spi.activation._ServerManagerImplBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.oa.poa.BadServerIdHandler"), "interface com.sun.corba.se.impl.oa.poa.BadServerIdHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("activate"), "method activate");
        check(mn.contains("active"), "method active");
        check(mn.contains("getActiveServers"), "method getActiveServers");
        check(mn.contains("getEndpoint"), "method getEndpoint");
        check(mn.contains("getORBNames"), "method getORBNames");
        check(mn.contains("getServerPortForType"), "method getServerPortForType");
        check(mn.contains("handle"), "method handle");
        check(mn.contains("install"), "method install");
        check(mn.contains("locateServer"), "method locateServer");
        check(mn.contains("locateServerForORB"), "method locateServerForORB");
        check(mn.contains("registerEndpoints"), "method registerEndpoints");
        check(mn.contains("shutdown"), "method shutdown");
        check(mn.contains("uninstall"), "method uninstall");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
