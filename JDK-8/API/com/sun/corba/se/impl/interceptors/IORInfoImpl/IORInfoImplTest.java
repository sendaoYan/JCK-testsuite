/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.IORInfoImpl conformance: type shape via reflection (headless-safe).
 * @run main IORInfoImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IORInfoImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.IORInfoImpl", false, IORInfoImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.legacy.interceptor.IORInfoExt"), "interface com.sun.corba.se.spi.legacy.interceptor.IORInfoExt");
        check(in.contains("org.omg.PortableInterceptor.IORInfo"), "interface org.omg.PortableInterceptor.IORInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("adapter_template"), "method adapter_template");
        check(mn.contains("add_ior_component"), "method add_ior_component");
        check(mn.contains("add_ior_component_to_profile"), "method add_ior_component_to_profile");
        check(mn.contains("current_factory"), "method current_factory");
        check(mn.contains("getObjectAdapter"), "method getObjectAdapter");
        check(mn.contains("getServerPort"), "method getServerPort");
        check(mn.contains("get_effective_policy"), "method get_effective_policy");
        check(mn.contains("manager_id"), "method manager_id");
        check(mn.contains("state"), "method state");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
