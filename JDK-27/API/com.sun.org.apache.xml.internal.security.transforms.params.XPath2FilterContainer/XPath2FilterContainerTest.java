/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.params.XPath2FilterContainer conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms.params:+open
 * @run main XPath2FilterContainerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class XPath2FilterContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.params.XPath2FilterContainer", false, XPath2FilterContainerTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.params.XPath2FilterContainer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(methods.contains("getXPathFilterStr"), "method:getXPathFilterStr");
        check(methods.contains("getXPathFilterTextNode"), "method:getXPathFilterTextNode");
        check(methods.contains("isIntersect"), "method:isIntersect");
        check(methods.contains("isSubtract"), "method:isSubtract");
        check(methods.contains("isUnion"), "method:isUnion");
        check(methods.contains("newInstance"), "method:newInstance");
        check(methods.contains("newInstanceIntersect"), "method:newInstanceIntersect");
        check(methods.contains("newInstanceSubtract"), "method:newInstanceSubtract");
        check(methods.contains("newInstanceUnion"), "method:newInstanceUnion");
        check(methods.contains("newInstances"), "method:newInstances");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("INTERSECT"), "field:INTERSECT");
        check(fields.contains("SUBTRACT"), "field:SUBTRACT");
        check(fields.contains("UNION"), "field:UNION");
        check(fields.contains("XPathFilter2NS"), "field:XPathFilter2NS");
        check(fields.contains("_TAG_XPATH2"), "field:_TAG_XPATH2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
