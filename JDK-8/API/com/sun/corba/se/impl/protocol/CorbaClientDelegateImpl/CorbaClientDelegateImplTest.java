/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.CorbaClientDelegateImpl conformance: type shape via reflection (headless-safe).
 * @run main CorbaClientDelegateImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaClientDelegateImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.CorbaClientDelegateImpl", false, CorbaClientDelegateImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.protocol.CorbaClientDelegate"), "superclass com.sun.corba.se.spi.protocol.CorbaClientDelegate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 21, "public declared method count");
        check(mn.contains("create_request"), "method create_request");
        check(mn.contains("duplicate"), "method duplicate");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getContactInfoList"), "method getContactInfoList");
        check(mn.contains("get_codebase"), "method get_codebase");
        check(mn.contains("get_interface_def"), "method get_interface_def");
        check(mn.contains("hash"), "method hash");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("is_a"), "method is_a");
        check(mn.contains("is_equivalent"), "method is_equivalent");
        check(mn.contains("is_local"), "method is_local");
        check(mn.contains("non_existent"), "method non_existent");
        check(mn.contains("orb"), "method orb");
        check(mn.contains("release"), "method release");
        check(mn.contains("releaseReply"), "method releaseReply");
        check(mn.contains("request"), "method request");
        check(mn.contains("servant_postinvoke"), "method servant_postinvoke");
        check(mn.contains("servant_preinvoke"), "method servant_preinvoke");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
