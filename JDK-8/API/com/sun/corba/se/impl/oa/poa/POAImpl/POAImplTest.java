/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAImpl conformance: type shape via reflection (headless-safe).
 * @run main POAImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAImpl", false, POAImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.oa.ObjectAdapterBase"), "superclass com.sun.corba.se.spi.oa.ObjectAdapterBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.POA"), "interface org.omg.PortableServer.POA");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 41, "public declared method count");
        check(mn.contains("activate_object"), "method activate_object");
        check(mn.contains("activate_object_with_id"), "method activate_object_with_id");
        check(mn.contains("create_POA"), "method create_POA");
        check(mn.contains("create_id_assignment_policy"), "method create_id_assignment_policy");
        check(mn.contains("create_id_uniqueness_policy"), "method create_id_uniqueness_policy");
        check(mn.contains("create_implicit_activation_policy"), "method create_implicit_activation_policy");
        check(mn.contains("create_lifespan_policy"), "method create_lifespan_policy");
        check(mn.contains("create_reference"), "method create_reference");
        check(mn.contains("create_reference_with_id"), "method create_reference_with_id");
        check(mn.contains("create_request_processing_policy"), "method create_request_processing_policy");
        check(mn.contains("create_servant_retention_policy"), "method create_servant_retention_policy");
        check(mn.contains("create_thread_policy"), "method create_thread_policy");
        check(mn.contains("deactivate_object"), "method deactivate_object");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("enter"), "method enter");
        check(mn.contains("exit"), "method exit");
        check(mn.contains("find_POA"), "method find_POA");
        check(mn.contains("getEffectivePolicy"), "method getEffectivePolicy");
        check(mn.contains("getInterfaces"), "method getInterfaces");
        check(mn.contains("getInvocationServant"), "method getInvocationServant");
        check(mn.contains("getLocalServant"), "method getLocalServant");
        check(mn.contains("getManagerId"), "method getManagerId");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("get_servant"), "method get_servant");
        check(mn.contains("get_servant_manager"), "method get_servant_manager");
        check(mn.contains("id"), "method id");
        check(mn.contains("id_to_reference"), "method id_to_reference");
        check(mn.contains("id_to_servant"), "method id_to_servant");
        check(mn.contains("reference_to_id"), "method reference_to_id");
        check(mn.contains("reference_to_servant"), "method reference_to_servant");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("servant_to_id"), "method servant_to_id");
        check(mn.contains("servant_to_reference"), "method servant_to_reference");
        check(mn.contains("set_servant"), "method set_servant");
        check(mn.contains("set_servant_manager"), "method set_servant_manager");
        check(mn.contains("the_POAManager"), "method the_POAManager");
        check(mn.contains("the_activator"), "method the_activator");
        check(mn.contains("the_children"), "method the_children");
        check(mn.contains("the_name"), "method the_name");
        check(mn.contains("the_parent"), "method the_parent");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
