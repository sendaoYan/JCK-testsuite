/*
 * @test
 * @summary javax.management.AttributeValueExp conformance: white-box structure + black-box behavior.
 * @run main AttributeValueExpTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class AttributeValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeValueExp");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(ValueExp.class.isAssignableFrom(c), "implements ValueExp");
        check(hasMethod(c, "getAttributeName"), "has getAttributeName");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
        check(hasMethod(c, "toString"), "has toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributeValueExp exp = new AttributeValueExp("VmName");
        check("VmName".equals(exp.getAttributeName()), "getAttributeName");
        check("VmName".equals(exp.toString()), "toString returns attribute name");

        // apply() resolves the attribute through the QueryEval thread-local MBeanServer.
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        QueryEval qe = new QueryEval() { };
        qe.setMBeanServer(mbs);
        ValueExp result = exp.apply(new ObjectName("java.lang:type=Runtime"));
        check(result instanceof StringValueExp, "apply returns StringValueExp for String attribute");
        String v = ((StringValueExp) result).getValue();
        check(v != null && v.length() > 0, "VmName attribute non-empty: " + v);

        // A non-existent attribute makes getAttribute return null, so apply() throws
        // BadAttributeValueExpException (the null result is not Number/String/Boolean).
        boolean threwMissing = false;
        try {
            new AttributeValueExp("NoSuchAttribute999").apply(new ObjectName("java.lang:type=Runtime"));
        } catch (BadAttributeValueExpException ex) { threwMissing = true; }
        check(threwMissing, "missing attribute throws BadAttributeValueExpException");

        // numeric attribute wraps into a NumericValueExp
        ValueExp num = new AttributeValueExp("ThreadCount").apply(new ObjectName("java.lang:type=Threading"));
        check(num != null, "numeric attribute applies without error");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
