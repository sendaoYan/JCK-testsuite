/*
 * @test
 * @summary javax.management.StringValueExp conformance: white-box structure + black-box behavior.
 * @run main StringValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class StringValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.StringValueExp");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(ValueExp.class.isAssignableFrom(c), "implements ValueExp");
        check(hasMethod(c, "getValue"), "has getValue");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StringValueExp s = new StringValueExp("hello");
        check("hello".equals(s.getValue()), "getValue");
        check("'hello'".equals(s.toString()), "toString single-quoted");

        StringValueExp empty = new StringValueExp("");
        check("".equals(empty.getValue()), "empty value");
        check("''".equals(empty.toString()), "empty toString");

        StringValueExp def = new StringValueExp();
        check(def.getValue() == null, "default constructor null value");

        // single quote escaped by doubling in toString
        StringValueExp quote = new StringValueExp("a'b");
        check("'a''b'".equals(quote.toString()), "single quote escaped");

        // apply() returns itself (identity), regardless of ObjectName
        ValueExp applied = s.apply(new ObjectName("d:k=v"));
        check(applied == s, "apply returns this");
        check(((StringValueExp) applied).getValue().equals("hello"), "apply preserves value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
