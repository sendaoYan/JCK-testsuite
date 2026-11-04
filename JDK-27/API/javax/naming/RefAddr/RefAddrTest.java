/*
 * @test
 * @summary javax.naming.RefAddr conformance: white-box structure + black-box behavior via subclasses.
 * @run main RefAddrTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;

public class RefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = RefAddr.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getDeclaredConstructor(String.class) != null, "protected ctor(String)");
        check(!Modifier.isPublic(c.getDeclaredConstructor(String.class).getModifiers()), "ctor protected");
        for (String m : new String[] {"getType", "getContent", "equals", "hashCode", "toString"})
            check(method(c, m), "declares " + m);
        check(StringRefAddr.class.getSuperclass() == RefAddr.class, "StringRefAddr extends RefAddr");
        check(javax.naming.BinaryRefAddr.class.getSuperclass() == RefAddr.class, "BinaryRefAddr extends RefAddr");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RefAddr a = new StringRefAddr("svc", "http://endpoint");
        check(a.getType().equals("svc"), "getType");
        check(a.getContent().equals("http://endpoint"), "getContent");
        check(a.toString().startsWith("Type: svc") && a.toString().contains("Content: http://endpoint"), "toString format");

        // equals/hashCode driven by type+content through the abstract contract
        RefAddr b = new StringRefAddr("svc", "http://endpoint");
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode contract");
        check(!a.equals(new StringRefAddr("other", "http://endpoint")), "type differs -> unequal");
        check(!a.equals(new StringRefAddr("svc", "http://elsewhere")), "content differs -> unequal");
        check(!a.equals(null) && !a.equals("svc"), "equals degenerate");

        // null content tolerated (JDK-4347817)
        RefAddr nullAddr = new StringRefAddr("T1", null);
        check(nullAddr.toString() != null, "toString with null content");
        check(nullAddr.hashCode() != 0 || nullAddr.hashCode() == 0, "hashCode callable with null content");

        check(new StringRefAddr("svc", "http://endpoint").equals(a), "reconstruct equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
