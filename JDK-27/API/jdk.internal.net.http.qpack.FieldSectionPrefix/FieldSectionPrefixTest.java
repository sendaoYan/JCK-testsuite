/*
 * @test
 * @summary jdk.internal.net.http.qpack.FieldSectionPrefix conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main FieldSectionPrefixTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FieldSectionPrefixTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.FieldSectionPrefix", false, FieldSectionPrefixTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.FieldSectionPrefix"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("base"), "method:base");
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("requiredInsertCount"), "method:requiredInsertCount");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
