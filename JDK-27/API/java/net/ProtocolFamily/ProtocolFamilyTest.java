/*
 * @test
 * @summary java.net.ProtocolFamily conformance: interface structure (exactly
 *          one abstract method name()), StandardProtocolFamily as the
 *          platform implementation with INET/INET6/UNIX (JDK 16+) instances,
 *          custom implementations with arbitrary names, and polymorphic use
 *          through the interface.
 * @run main ProtocolFamilyTest
 */

import java.lang.reflect.*;
import java.net.*;

public class ProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProtocolFamily");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        Method name = c.getMethod("name");
        check(name != null, "name()");
        check(Modifier.isAbstract(name.getModifiers()), "name abstract");
        check(name.getReturnType() == String.class, "name returns String");
        check(name.getParameterTypes().length == 0, "name no args");
        int declared = 0;
        for (Method m : c.getDeclaredMethods()) declared++;
        check(declared == 1, "exactly one declared method, got " + declared);
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
        ProtocolFamily f2 = new CustomFamily("CUSTOM");
        check(f.name().equals(f2.name()), "same name");
        check(f != f2, "distinct identities");
        ProtocolFamily n = new CustomFamily(null);
        check(n.name() == null, "null name passes through");
        ProtocolFamily e = new CustomFamily("");
        check("".equals(e.name()), "empty name passes through");
    }

    static void testStandardImplementations() throws Exception {
        check(StandardProtocolFamily.INET instanceof ProtocolFamily, "INET implements");
        check(StandardProtocolFamily.INET6 instanceof ProtocolFamily, "INET6 implements");
        check(StandardProtocolFamily.UNIX instanceof ProtocolFamily, "UNIX implements");
        ProtocolFamily inet = StandardProtocolFamily.INET;
        ProtocolFamily inet6 = StandardProtocolFamily.INET6;
        ProtocolFamily unix = StandardProtocolFamily.UNIX;
        check("INET".equals(inet.name()), "INET name via interface");
        check("INET6".equals(inet6.name()), "INET6 name via interface");
        check("UNIX".equals(unix.name()), "UNIX name via interface");
        check(!inet.name().equals(inet6.name()), "INET vs INET6 names differ");
        check(!unix.name().equals(inet.name()), "UNIX vs INET names differ");
        check(inet != inet6 && inet6 != unix, "distinct constants");
        // JDK 16+ defines three families.
        StandardProtocolFamily[] vals = StandardProtocolFamily.values();
        check(vals.length == 3, "JDK-27 has exactly 3 families, got " + vals.length);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (StandardProtocolFamily v : vals) names.add(((ProtocolFamily) v).name());
        check(names.contains("INET") && names.contains("INET6") && names.contains("UNIX"),
                "names cover INET/INET6/UNIX");
        ProtocolFamily custom = new CustomFamily("UNIX");
        check(custom.name().equals(StandardProtocolFamily.UNIX.name()),
                "custom may reuse the standard name string");
    }

    static void testPolymorphicUsage() throws Exception {
        java.util.List<String> rendered = new java.util.ArrayList<String>();
        for (ProtocolFamily f : new ProtocolFamily[]{StandardProtocolFamily.INET,
                StandardProtocolFamily.INET6, StandardProtocolFamily.UNIX, new CustomFamily("X")}) {
            rendered.add(f.name());
        }
        check(rendered.size() == 4, "four families rendered");
        check(rendered.get(0).equals("INET"), "first INET");
        check(rendered.get(1).equals("INET6"), "second INET6");
        check(rendered.get(2).equals("UNIX"), "third UNIX");
        check(rendered.get(3).equals("X"), "fourth custom");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCustomImplementation();
        testStandardImplementations();
        testPolymorphicUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
