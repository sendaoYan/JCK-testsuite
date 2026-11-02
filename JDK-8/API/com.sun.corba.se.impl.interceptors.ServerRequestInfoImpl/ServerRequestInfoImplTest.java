/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.ServerRequestInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main ServerRequestInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ServerRequestInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.ServerRequestInfoImpl", false, ServerRequestInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.interceptors.RequestInfoImpl"), "superclass com.sun.corba.se.impl.interceptors.RequestInfoImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableInterceptor.ServerRequestInfo"), "interface org.omg.PortableInterceptor.ServerRequestInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 22, "public declared method count");
        check(mn.contains("adapter_id"), "method adapter_id");
        check(mn.contains("adapter_name"), "method adapter_name");
        check(mn.contains("add_reply_service_context"), "method add_reply_service_context");
        check(mn.contains("arguments"), "method arguments");
        check(mn.contains("contexts"), "method contexts");
        check(mn.contains("exceptions"), "method exceptions");
        check(mn.contains("forward_reference"), "method forward_reference");
        check(mn.contains("get_reply_service_context"), "method get_reply_service_context");
        check(mn.contains("get_request_service_context"), "method get_request_service_context");
        check(mn.contains("get_server_policy"), "method get_server_policy");
        check(mn.contains("object_id"), "method object_id");
        check(mn.contains("operation"), "method operation");
        check(mn.contains("operation_context"), "method operation_context");
        check(mn.contains("orb_id"), "method orb_id");
        check(mn.contains("request_id"), "method request_id");
        check(mn.contains("response_expected"), "method response_expected");
        check(mn.contains("result"), "method result");
        check(mn.contains("sending_exception"), "method sending_exception");
        check(mn.contains("server_id"), "method server_id");
        check(mn.contains("set_slot"), "method set_slot");
        check(mn.contains("target_is_a"), "method target_is_a");
        check(mn.contains("target_most_derived_interface"), "method target_most_derived_interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
