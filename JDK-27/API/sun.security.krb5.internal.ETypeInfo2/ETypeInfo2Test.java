/*
 * @test
 * @summary sun.security.krb5.internal.ETypeInfo2 conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main ETypeInfo2Test
 */
import java.lang.reflect.*;
import java.util.*;
public class ETypeInfo2Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ETypeInfo2", false, ETypeInfo2Test.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ETypeInfo2"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        check(methods.contains("clone"), "method:clone");
        check(methods.contains("getEType"), "method:getEType");
        check(methods.contains("getParams"), "method:getParams");
        check(methods.contains("getSalt"), "method:getSalt");
        check(methods.contains("hasParams"), "method:hasParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
