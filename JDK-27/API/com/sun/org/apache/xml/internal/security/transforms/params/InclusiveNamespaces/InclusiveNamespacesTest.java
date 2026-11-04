/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.params.InclusiveNamespaces conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms.params:+open
 * @run main InclusiveNamespacesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class InclusiveNamespacesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.params.InclusiveNamespaces", false, InclusiveNamespacesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.params.InclusiveNamespaces"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.ElementProxy"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("com.sun.org.apache.xml.internal.security.transforms.TransformParam"), "iface:com.sun.org.apache.xml.internal.security.transforms.TransformParam");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getBaseLocalName"), "method:getBaseLocalName");
        check(methods.contains("getBaseNamespace"), "method:getBaseNamespace");
        check(methods.contains("getInclusiveNamespaces"), "method:getInclusiveNamespaces");
        check(methods.contains("prefixStr2Set"), "method:prefixStr2Set");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ExclusiveCanonicalizationNamespace"), "field:ExclusiveCanonicalizationNamespace");
        check(fields.contains("_ATT_EC_PREFIXLIST"), "field:_ATT_EC_PREFIXLIST");
        check(fields.contains("_TAG_EC_INCLUSIVENAMESPACES"), "field:_TAG_EC_INCLUSIVENAMESPACES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
