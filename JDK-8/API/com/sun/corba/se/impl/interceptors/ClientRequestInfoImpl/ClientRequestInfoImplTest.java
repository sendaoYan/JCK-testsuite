/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.ClientRequestInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main ClientRequestInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ClientRequestInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.ClientRequestInfoImpl", false, ClientRequestInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.interceptors.RequestInfoImpl"), "superclass com.sun.corba.se.impl.interceptors.RequestInfoImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableInterceptor.ClientRequestInfo"), "interface org.omg.PortableInterceptor.ClientRequestInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 21, "public declared method count");
        check(mn.contains("add_request_service_context"), "method add_request_service_context");
        check(mn.contains("arguments"), "method arguments");
        check(mn.contains("connection"), "method connection");
        check(mn.contains("contexts"), "method contexts");
        check(mn.contains("effective_profile"), "method effective_profile");
        check(mn.contains("effective_target"), "method effective_target");
        check(mn.contains("exceptions"), "method exceptions");
        check(mn.contains("forward_reference"), "method forward_reference");
        check(mn.contains("get_effective_component"), "method get_effective_component");
        check(mn.contains("get_effective_components"), "method get_effective_components");
        check(mn.contains("get_reply_service_context"), "method get_reply_service_context");
        check(mn.contains("get_request_policy"), "method get_request_policy");
        check(mn.contains("get_request_service_context"), "method get_request_service_context");
        check(mn.contains("operation"), "method operation");
        check(mn.contains("operation_context"), "method operation_context");
        check(mn.contains("received_exception"), "method received_exception");
        check(mn.contains("received_exception_id"), "method received_exception_id");
        check(mn.contains("request_id"), "method request_id");
        check(mn.contains("response_expected"), "method response_expected");
        check(mn.contains("result"), "method result");
        check(mn.contains("target"), "method target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
