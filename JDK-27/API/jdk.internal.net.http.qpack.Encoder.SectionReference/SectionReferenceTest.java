/*
 * @test
 * @summary jdk.internal.net.http.qpack.Encoder.SectionReference conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main SectionReferenceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SectionReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.Encoder$SectionReference", false, SectionReferenceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.Encoder$SectionReference"), "name");
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
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("fullyAcked"), "method:fullyAcked");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("max"), "method:max");
        check(methods.contains("min"), "method:min");
        check(methods.contains("noReferences"), "method:noReferences");
        check(methods.contains("of"), "method:of");
        check(methods.contains("reduce"), "method:reduce");
        check(methods.contains("referencesEntries"), "method:referencesEntries");
        check(methods.contains("singleReference"), "method:singleReference");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
