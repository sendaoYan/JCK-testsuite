/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.ORBInitInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main ORBInitInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ORBInitInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.ORBInitInfoImpl", false, ORBInitInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.interceptor.ORBInitInfoExt"), "interface com.sun.corba.se.spi.legacy.interceptor.ORBInitInfoExt");
        check(in.contains("org.omg.PortableInterceptor.ORBInitInfo"), "interface org.omg.PortableInterceptor.ORBInitInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 14, "public declared method count");
        check(mn.contains("add_client_request_interceptor"), "method add_client_request_interceptor");
        check(mn.contains("add_client_request_interceptor_with_policy"), "method add_client_request_interceptor_with_policy");
        check(mn.contains("add_ior_interceptor"), "method add_ior_interceptor");
        check(mn.contains("add_ior_interceptor_with_policy"), "method add_ior_interceptor_with_policy");
        check(mn.contains("add_server_request_interceptor"), "method add_server_request_interceptor");
        check(mn.contains("add_server_request_interceptor_with_policy"), "method add_server_request_interceptor_with_policy");
        check(mn.contains("allocate_slot_id"), "method allocate_slot_id");
        check(mn.contains("arguments"), "method arguments");
        check(mn.contains("codec_factory"), "method codec_factory");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("orb_id"), "method orb_id");
        check(mn.contains("register_initial_reference"), "method register_initial_reference");
        check(mn.contains("register_policy_factory"), "method register_policy_factory");
        check(mn.contains("resolve_initial_references"), "method resolve_initial_references");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
