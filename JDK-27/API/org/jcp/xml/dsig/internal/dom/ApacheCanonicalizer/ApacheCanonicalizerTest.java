/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main ApacheCanonicalizerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheCanonicalizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer", false, ApacheCanonicalizerTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheCanonicalizer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.xml.crypto.dsig.TransformService"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("getParameterSpec"), "method:getParameterSpec");
        check(methods.contains("init"), "method:init");
        check(methods.contains("isFeatureSupported"), "method:isFeatureSupported");
        check(methods.contains("marshalParams"), "method:marshalParams");
        check(methods.contains("transform"), "method:transform");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("canonicalizer"), "field:canonicalizer");
        check(fields.contains("inclusiveNamespaces"), "field:inclusiveNamespaces");
        check(fields.contains("ownerDoc"), "field:ownerDoc");
        check(fields.contains("params"), "field:params");
        check(fields.contains("transformElem"), "field:transformElem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
