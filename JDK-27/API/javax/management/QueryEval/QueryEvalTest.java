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
        testEvalThreadLocal();
        testEvalInheritance();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch e): ThreadLocal server context, inheritance,
    //      cleanup after server-side queries, anonymous QueryEval QueryExp ----

    public static interface EvalCounterMBean { int getCount(); }

    public static class EvalCounter implements EvalCounterMBean {
        public int getCount() { return 1; }
    }

    static class ContextExp extends QueryEval implements QueryExp {
        boolean sawServer;
        public boolean apply(ObjectName name) {
            sawServer = QueryEval.getMBeanServer() != null;
            return sawServer;
        }
    }

    static void testEvalThreadLocal() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        server.registerMBean(new EvalCounter(), new ObjectName("jck.eval", "k", "c"));
        // server-side queries inject the query context during evaluation and
        // restore the previous value afterwards
        MBeanServer before = QueryEval.getMBeanServer();
        ContextExp exp = new ContextExp();
        server.queryNames(null, exp);
        check(exp.sawServer, "server injects query context during queryNames evaluation");
        check(QueryEval.getMBeanServer() == before, "previous value restored after queryNames");
        QueryEval eval = new QueryEval() { };
        try {
            eval.setMBeanServer(server);
            check(QueryEval.getMBeanServer() == server, "getMBeanServer returns what was set (same thread)");
            ContextExp direct = new ContextExp();
            check(direct.apply(new ObjectName("d", "k", "v")), "QueryEval subclass sees server inside apply");
            check(direct.sawServer, "context visible inside apply");
            server.queryNames(null, Query.eq(Query.attr("Count"), Query.value(1)));
            check(QueryEval.getMBeanServer() == server, "previous value restored after server queryNames");
            server.queryMBeans(null, Query.eq(Query.attr("Count"), Query.value(1)));
            check(QueryEval.getMBeanServer() == server, "previous value restored after queryMBeans");
        } finally {
            eval.setMBeanServer(null);
        }
        check(QueryEval.getMBeanServer() == null, "setMBeanServer(null) clears the context");
    }

    static void testEvalInheritance() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        QueryEval eval = new QueryEval() { };
        eval.setMBeanServer(server);
        final MBeanServer[] child = new MBeanServer[1];
        Thread t = new Thread() { public void run() { child[0] = QueryEval.getMBeanServer(); } };
        t.start();
        t.join();
        check(child[0] == server, "child thread inherits the query context (InheritableThreadLocal)");
        eval.setMBeanServer(null);
        final MBeanServer[] child2 = new MBeanServer[1];
        Thread t2 = new Thread() { public void run() { child2[0] = QueryEval.getMBeanServer(); } };
        t2.start();
        t2.join();
        check(child2[0] == null, "cleared context is not inherited");
    }
}
