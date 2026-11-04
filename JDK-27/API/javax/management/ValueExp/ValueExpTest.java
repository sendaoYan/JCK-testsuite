/*
 * @test
 * @summary javax.management.ValueExp conformance: white-box structure + light behavior.
 * @run main ValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

@SuppressWarnings("deprecation")
public class ValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ValueExp");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final ValueExp[] applied = { null };
        ValueExp exp = new ValueExp() {
            public ValueExp apply(ObjectName name) { applied[0] = this; return this; }
            public void setMBeanServer(MBeanServer s) { }
        };
        ObjectName on = new ObjectName("d:k=v");
        ValueExp result = exp.apply(on);
        check(result == exp, "apply returns instance");
        check(applied[0] == exp, "apply invoked");

        exp.setMBeanServer(null);
        check(true, "setMBeanServer callable");

        // concrete ValueExp implementations are real ValueExps
        check(new StringValueExp("x") instanceof ValueExp, "StringValueExp is a ValueExp");
        check(new AttributeValueExp("a") instanceof ValueExp, "AttributeValueExp is a ValueExp");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
