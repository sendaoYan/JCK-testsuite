/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.RequestInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main RequestInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.RequestInfoImpl", false, RequestInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.interceptor.RequestInfoExt"), "interface com.sun.corba.se.spi.legacy.interceptor.RequestInfoExt");
        check(in.contains("org.omg.PortableInterceptor.RequestInfo"), "interface org.omg.PortableInterceptor.RequestInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 15, "public declared method count");
        check(mn.contains("arguments"), "method arguments");
        check(mn.contains("connection"), "method connection");
        check(mn.contains("contexts"), "method contexts");
        check(mn.contains("exceptions"), "method exceptions");
        check(mn.contains("forward_reference"), "method forward_reference");
        check(mn.contains("get_reply_service_context"), "method get_reply_service_context");
        check(mn.contains("get_request_service_context"), "method get_request_service_context");
        check(mn.contains("get_slot"), "method get_slot");
        check(mn.contains("operation"), "method operation");
        check(mn.contains("operation_context"), "method operation_context");
        check(mn.contains("reply_status"), "method reply_status");
        check(mn.contains("request_id"), "method request_id");
        check(mn.contains("response_expected"), "method response_expected");
        check(mn.contains("result"), "method result");
        check(mn.contains("sync_scope"), "method sync_scope");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
