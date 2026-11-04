/*
 * @test
 * @summary com.sun.corba.se.impl.naming.cosnaming.NamingContextImpl conformance: type shape via reflection (headless-safe).
 * @run main NamingContextImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NamingContextImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.cosnaming.NamingContextImpl", false, NamingContextImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CosNaming.NamingContextExtPOA"), "superclass org.omg.CosNaming.NamingContextExtPOA");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore"), "interface com.sun.corba.se.impl.naming.cosnaming.NamingContextDataStore");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 19, "public declared method count");
        check(mn.contains("bind"), "method bind");
        check(mn.contains("bind_context"), "method bind_context");
        check(mn.contains("bind_new_context"), "method bind_new_context");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("doBind"), "method doBind");
        check(mn.contains("doResolve"), "method doResolve");
        check(mn.contains("doUnbind"), "method doUnbind");
        check(mn.contains("getNSPOA"), "method getNSPOA");
        check(mn.contains("list"), "method list");
        check(mn.contains("nameToString"), "method nameToString");
        check(mn.contains("new_context"), "method new_context");
        check(mn.contains("rebind"), "method rebind");
        check(mn.contains("rebind_context"), "method rebind_context");
        check(mn.contains("resolve"), "method resolve");
        check(mn.contains("resolve_str"), "method resolve_str");
        check(mn.contains("to_name"), "method to_name");
        check(mn.contains("to_string"), "method to_string");
        check(mn.contains("to_url"), "method to_url");
        check(mn.contains("unbind"), "method unbind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
