/*
 * @test
 * @summary javax.naming.StringRefAddr conformance: white-box structure + black-box behavior.
 * @run main StringRefAddrTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;

public class StringRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = StringRefAddr.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == RefAddr.class, "extends RefAddr");
        Constructor<?> ct = c.getDeclaredConstructor(String.class, String.class);
        check(Modifier.isPublic(ct.getModifiers()), "public ctor(String,String)");
        for (String m : new String[] {"getContent", "equals", "hashCode", "toString"})
            check(method(c, m), "inherits/overrides " + m);
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringRefAddr a = new StringRefAddr("addrType", "content");
        check(a.getType().equals("addrType"), "type");
        check(a.getContent() instanceof String, "content type");
        check(((String) a.getContent()).equals("content"), "content value");

        // value semantics: equals compares type + string content
        check(a.equals(new StringRefAddr("addrType", "content")), "equals same");
        check(!a.equals(new StringRefAddr("addrType", "other")), "content mismatch");
        check(!a.equals(new StringRefAddr("other", "content")), "type mismatch");
        check(a.hashCode() == new StringRefAddr("addrType", "content").hashCode(), "hashCode equal pair");
        check(a.toString().equals("Type: addrType\nContent: content") || a.toString().startsWith("Type: addrType"),
                "toString shape");

        // null content is legal (JDK-4347817 regression)
        StringRefAddr n = new StringRefAddr("T", null);
        check(n.getContent() == null, "null content preserved");
        check(n.toString() != null, "toString null-safe");
        check(n.equals(new StringRefAddr("T", null)), "null equals null");
        check(!n.equals(a), "null vs non-null");

        // empty-string content differs from null
        StringRefAddr e = new StringRefAddr("T", "");
        check(!e.equals(n), "empty != null content");

        // same instance used twice in a Reference stays addressable by type
        javax.naming.Reference ref = new javax.naming.Reference("C", a);
        check(ref.get("addrType") == a, "retrieved by type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
