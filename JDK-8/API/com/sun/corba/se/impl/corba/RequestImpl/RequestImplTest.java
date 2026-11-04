/*
 * @test
 * @summary com.sun.corba.se.impl.corba.RequestImpl conformance: type shape via reflection (headless-safe).
 * @run main RequestImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.RequestImpl", false, RequestImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.Request"), "superclass org.omg.CORBA.Request");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 23, "public declared method count");
        check(mn.contains("add_exception"), "method add_exception");
        check(mn.contains("add_in_arg"), "method add_in_arg");
        check(mn.contains("add_inout_arg"), "method add_inout_arg");
        check(mn.contains("add_named_in_arg"), "method add_named_in_arg");
        check(mn.contains("add_named_inout_arg"), "method add_named_inout_arg");
        check(mn.contains("add_named_out_arg"), "method add_named_out_arg");
        check(mn.contains("add_out_arg"), "method add_out_arg");
        check(mn.contains("arguments"), "method arguments");
        check(mn.contains("contexts"), "method contexts");
        check(mn.contains("ctx"), "method ctx");
        check(mn.contains("env"), "method env");
        check(mn.contains("exceptions"), "method exceptions");
        check(mn.contains("get_response"), "method get_response");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("operation"), "method operation");
        check(mn.contains("poll_response"), "method poll_response");
        check(mn.contains("result"), "method result");
        check(mn.contains("return_value"), "method return_value");
        check(mn.contains("send_deferred"), "method send_deferred");
        check(mn.contains("send_oneway"), "method send_oneway");
        check(mn.contains("set_return_type"), "method set_return_type");
        check(mn.contains("target"), "method target");
        check(mn.contains("unmarshalReply"), "method unmarshalReply");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
