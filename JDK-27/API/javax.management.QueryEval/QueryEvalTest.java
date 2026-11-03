/*
 * @test
 * @summary javax.management.QueryEval conformance: white-box structure + black-box behavior.
 * @run main QueryEvalTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class QueryEvalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.QueryEval");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "setMBeanServer"), "has setMBeanServer");
        check(hasMethod(c, "getMBeanServer"), "has static getMBeanServer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        QueryEval eval = new QueryEval() { };
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // before set, the current thread has no MBeanServer
        check(QueryEval.getMBeanServer() == null, "getMBeanServer initially null");

        // setMBeanServer sets a thread-local value
        eval.setMBeanServer(mbs);
        check(QueryEval.getMBeanServer() == mbs, "getMBeanServer reflects setMBeanServer");

        // it drives an AttributeValueExp.apply evaluation
        ValueExp result = new AttributeValueExp("VmName").apply(new ObjectName("java.lang:type=Runtime"));
        check(result instanceof StringValueExp, "AttributeValueExp.apply uses QueryEval MBeanServer");
        check(((StringValueExp) result).getValue() != null, "apply returns a value");

        // QueryEval instance does not affect another fresh thread's default (main thread state only)
        check(QueryEval.getMBeanServer() == mbs, "thread-local persists on same thread");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
