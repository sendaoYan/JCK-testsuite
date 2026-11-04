/*
 * @test
 * @summary java.net.ProtocolFamily conformance: interface structure (exactly
 *          one abstract method name()), StandardProtocolFamily as the
 *          platform implementation with INET/INET6 instances, custom
 *          implementations with arbitrary names, and polymorphic use
 *          through the interface.
 * @run main ProtocolFamilyTest
 */


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.util.HashSet;
import java.util.Set;

public class ProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolFamily");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        Method name = c.getMethod("name");
        check(name != null, "name()");
        check(Modifier.isAbstract(name.getModifiers()), "name abstract");
        check(name.getReturnType() == String.class, "name returns String");
        check(name.getParameterTypes().length == 0, "name no args");
        int declared = 0;
        for (Method m : c.getDeclaredMethods()) declared++;
        check(declared == 1, "exactly one declared method, got " + declared);
        // No constant or field members.
        check(c.getFields().length == 0, "no fields");
    }

    static class CustomFamily implements ProtocolFamily {
        private final String n;
        CustomFamily(String n) { this.n = n; }
        public String name() { return n; }
    }

    static void testCustomImplementation() throws Exception {
        ProtocolFamily f = new CustomFamily("CUSTOM");
        check("CUSTOM".equals(f.name()), "custom name");
        check(f instanceof ProtocolFamily, "custom is ProtocolFamily");
        // Distinct implementations with the same name are distinct objects.
        ProtocolFamily f2 = new CustomFamily("CUSTOM");
        check(f.name().equals(f2.name()), "same name");
        check(f != f2, "distinct identities");
        // Null names pass through (interface imposes no validation).
        ProtocolFamily n = new CustomFamily(null);
        check(n.name() == null, "null name passes through");
        // Empty name likewise.
        ProtocolFamily e = new CustomFamily("");
        check("".equals(e.name()), "empty name passes through");
    }

    static void testStandardImplementations() throws Exception {
        check(StandardProtocolFamily.INET instanceof ProtocolFamily, "INET implements");
        check(StandardProtocolFamily.INET6 instanceof ProtocolFamily, "INET6 implements");
        ProtocolFamily inet = StandardProtocolFamily.INET;
        ProtocolFamily inet6 = StandardProtocolFamily.INET6;
        check("INET".equals(inet.name()), "INET name via interface");
        check("INET6".equals(inet6.name()), "INET6 name via interface");
        check(!inet.name().equals(inet6.name()), "distinct names");
        check(inet != inet6, "distinct constants");
        // JDK-8 defines exactly the two families (JDK 16 adds UNIX).
        StandardProtocolFamily[] vals = StandardProtocolFamily.values();
        check(vals.length == 2, "JDK-8 has exactly 2 families, got " + vals.length);
        Set<String> names = new HashSet<String>();
        for (StandardProtocolFamily v : vals) names.add(((ProtocolFamily) v).name());
        check(names.contains("INET") && names.contains("INET6"), "names cover INET/INET6");
        // Custom and standard coexist.
        ProtocolFamily custom = new CustomFamily("INET");
        check(custom.name().equals(StandardProtocolFamily.INET.name()),
                "custom may reuse the standard name string");
    }

    static void testPolymorphicUsage() throws Exception {
        java.util.List<String> rendered = new java.util.ArrayList<String>();
        for (ProtocolFamily f : new ProtocolFamily[]{StandardProtocolFamily.INET,
                StandardProtocolFamily.INET6, new CustomFamily("X")}) {
            rendered.add(f.name());
        }
        check(rendered.size() == 3, "three families rendered");
        check(rendered.get(0).equals("INET"), "first INET");
        check(rendered.get(1).equals("INET6"), "second INET6");
        check(rendered.get(2).equals("X"), "third custom");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolFamily");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        testWhiteBox();
        testCustomImplementation();
        testStandardImplementations();
        testPolymorphicUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
