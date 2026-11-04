/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Policy conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main PolicyTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Policy", false, PolicyTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.Policy"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("disabledAlgs"), "method:disabledAlgs");
        check(methods.contains("disabledReferenceUriSchemes"), "method:disabledReferenceUriSchemes");
        check(methods.contains("maxReferences"), "method:maxReferences");
        check(methods.contains("maxTransforms"), "method:maxTransforms");
        check(methods.contains("minKeySize"), "method:minKeySize");
        check(methods.contains("restrictAlg"), "method:restrictAlg");
        check(methods.contains("restrictDuplicateIds"), "method:restrictDuplicateIds");
        check(methods.contains("restrictKey"), "method:restrictKey");
        check(methods.contains("restrictNumReferences"), "method:restrictNumReferences");
        check(methods.contains("restrictNumTransforms"), "method:restrictNumTransforms");
        check(methods.contains("restrictReferenceUriScheme"), "method:restrictReferenceUriScheme");
        check(methods.contains("restrictRetrievalMethodLoops"), "method:restrictRetrievalMethodLoops");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
