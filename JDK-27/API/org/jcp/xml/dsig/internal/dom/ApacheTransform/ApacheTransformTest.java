/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.ApacheTransform conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main ApacheTransformTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ApacheTransformTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheTransform", false, ApacheTransformTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.ApacheTransform"), "name");
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
        check(fields.contains("ownerDoc"), "field:ownerDoc");
        check(fields.contains("params"), "field:params");
        check(fields.contains("transformElem"), "field:transformElem");
        deepen();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ---- deepening: exact member face + behavior (contract lock) ----
    static void deepen() throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.ApacheTransform");
        check("javax.xml.crypto.dsig.TransformService".equals(c.getSuperclass().getName()), "superclass javax.xml.crypto.dsig.TransformService");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.isEmpty(), "no directly declared interfaces");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !java.lang.reflect.Modifier.isFinal(c.getModifiers()), "modifier set [public abstract]");
        java.util.Set<String> names = new java.util.HashSet<String>();
        java.util.Set<String> staticNames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) {
                names.add(m.getName());
                if (java.lang.reflect.Modifier.isStatic(mod)) staticNames.add(m.getName());
            }
        }
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList("getParameterSpec", "init", "isFeatureSupported", "marshalParams", "transform"))),
                "exact public/protected method face (5 names): " + names);
        java.util.Set<String> ctorFace = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPrivate(k.getModifiers())) continue;
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getName()).append(",");
            ctorFace.add(sb.toString());
        }
        check(ctorFace.size() == 1, "exactly 1 declared constructors, got " + ctorFace.size());
        check(ctorFace.contains(""), "constructor(no-arg) present");
        // black-box behavior
        javax.xml.crypto.dsig.TransformService b64 =
                javax.xml.crypto.dsig.TransformService.getInstance(javax.xml.crypto.dsig.Transform.BASE64, "DOM");
        check(b64 instanceof org.jcp.xml.dsig.internal.dom.ApacheTransform,
                "the registered BASE64 transform service extends ApacheTransform");
        javax.xml.crypto.dsig.TransformService xslt =
                javax.xml.crypto.dsig.TransformService.getInstance(javax.xml.crypto.dsig.Transform.XSLT, "DOM");
        check(xslt instanceof org.jcp.xml.dsig.internal.dom.ApacheTransform,
                "the registered XSLT transform service also extends ApacheTransform");
        check(!org.jcp.xml.dsig.internal.dom.ApacheTransform.class.equals(b64.getClass()),
                "ApacheTransform itself stays abstract");
    }
}
