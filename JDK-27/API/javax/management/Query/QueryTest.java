/*
 * @test
 * @summary javax.management.Query conformance: white-box structure + black-box behavior.
 * @run main QueryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class QueryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
        void setCount(int c);
        String getName();
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        private String name = "thing";
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public String getName() { return name; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.Query");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(hasMethod(c, "eq"), "has eq");
        check(hasMethod(c, "gt"), "has gt");
        check(hasMethod(c, "and"), "has and");
        check(hasMethod(c, "or"), "has or");
        check(hasMethod(c, "not"), "has not");
        check(hasMethod(c, "attr"), "has attr");
        check(hasMethod(c, "value"), "has value");
        check(Query.GT == 0 && Query.LT == 1 && Query.GE == 2 && Query.LE == 3 && Query.EQ == 4, "comparison constants");
        check(Query.PLUS == 0 && Query.MINUS == 1 && Query.TIMES == 2 && Query.DIV == 3, "arithmetic constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // factory methods produce non-null QueryExp/ValueExp objects
        AttributeValueExp attr = Query.attr("Count");
        check("Count".equals(attr.getAttributeName()), "attr name");
        check(Query.attr("java.lang.Integer", "Count").getAttributeName().equals("Count"), "attr(className,name) name");
        check(Query.classattr() instanceof AttributeValueExp, "classattr returns AttributeValueExp");
        check(Query.value("x") instanceof StringValueExp, "value(String) returns StringValueExp");
        check(Query.value(3) instanceof ValueExp, "value(int) returns ValueExp");
        check(Query.value(3L) instanceof ValueExp, "value(long) returns ValueExp");
        check(Query.value(3.5) instanceof ValueExp, "value(double) returns ValueExp");
        check(Query.value(true) instanceof ValueExp, "value(boolean) returns ValueExp");
        check(Query.value(Integer.valueOf(3)) instanceof ValueExp, "value(Number) returns ValueExp");

        QueryExp eq = Query.eq(Query.attr("Count"), Query.value(7));
        QueryExp gt = Query.gt(Query.attr("Count"), Query.value(1));
        QueryExp geq = Query.geq(Query.attr("Count"), Query.value(7));
        QueryExp lt = Query.lt(Query.attr("Count"), Query.value(100));
        QueryExp leq = Query.leq(Query.attr("Count"), Query.value(7));
        check(eq != null && gt != null && geq != null && lt != null && leq != null, "relational factories non-null");

        QueryExp and = Query.and(eq, lt);
        QueryExp or = Query.or(eq, gt);
        QueryExp not = Query.not(eq);
        QueryExp between = Query.between(Query.attr("Count"), Query.value(1), Query.value(10));
        QueryExp match = Query.match(Query.attr("Name"), Query.value("th*"));
        QueryExp in = Query.in(Query.attr("Count"), new ValueExp[]{Query.value(7), Query.value(8)});
        QueryExp initSub = Query.initialSubString(Query.attr("Name"), Query.value("th"));
        QueryExp anySub = Query.anySubString(Query.attr("Name"), Query.value("hin"));
        QueryExp finSub = Query.finalSubString(Query.attr("Name"), Query.value("ng"));
        QueryExp isInstance = Query.isInstanceOf(Query.value("java.lang.String"));
        check(and != null && or != null && not != null && between != null && match != null
                && in != null && initSub != null && anySub != null && finSub != null && isInstance != null,
                "combinatorial factories non-null");

        // arithmetic value expressions
        check(Query.plus(Query.value(1), Query.value(2)) instanceof ValueExp, "plus");
        check(Query.minus(Query.value(5), Query.value(2)) instanceof ValueExp, "minus");
        check(Query.times(Query.value(2), Query.value(3)) instanceof ValueExp, "times");
        check(Query.div(Query.value(6), Query.value(3)) instanceof ValueExp, "div");

        // behavioral evaluation against a registered standard MBean
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=QueryTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        try {
            Set<ObjectName> eqRes = mbs.queryNames(new ObjectName("jck.test:*"), eq);
            check(eqRes.contains(on), "eq matches count==7");
            check(!mbs.queryNames(new ObjectName("jck.test:*"), Query.eq(Query.attr("Count"), Query.value(8))).contains(on),
                    "eq does not match count==8");

            check(mbs.queryNames(new ObjectName("jck.test:*"), gt).contains(on), "gt matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), geq).contains(on), "geq matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), lt).contains(on), "lt matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), leq).contains(on), "leq matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), and).contains(on), "and matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), between).contains(on), "between matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), in).contains(on), "in matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), match).contains(on), "match matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), initSub).contains(on), "initialSubString matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), anySub).contains(on), "anySubString matches");
            check(mbs.queryNames(new ObjectName("jck.test:*"), finSub).contains(on), "finalSubString matches");
            check(!mbs.queryNames(new ObjectName("jck.test:*"), not).contains(on), "not eq excludes match");

            // count() attribute count via getMBeanCount
            check(mbs.getMBeanCount() > 0, "platform server has MBeans");
        } finally {
            mbs.unregisterMBean(on);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testQueryFiltering();
        System.out.println("PASS (" + passed + " assertions)");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepened (batch e): real query filtering against registered MBeans:
    //      comparison boundaries, boolean logic, string matching, arithmetic,
    //      isInstanceOf/classattr, class-qualified attr, silent negatives ----

    public static interface QCounterMBean {
        int getCount(); void setCount(int v); void reset(); int add(int x); boolean isReady();
    }

    public static class QCounter implements QCounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { return count + x; }
        public boolean isReady() { return true; }
    }

    public static interface QNamedMBean { String getName(); void setName(String v); }

    public static class QNamed implements QNamedMBean {
        private String name = "alpha";
        public String getName() { return name; }
        public void setName(String v) { name = v; }
    }

    static Set<ObjectName> qnames(ObjectName... ns) {
        return new HashSet<ObjectName>(Arrays.asList(ns));
    }

    static void testQueryFiltering() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        QCounter a5 = new QCounter(); a5.setCount(5);
        QCounter b6 = new QCounter(); b6.setCount(6);
        ObjectName na = new ObjectName("jck.q", "name", "a");
        ObjectName nb = new ObjectName("jck.q", "name", "b");
        ObjectName nn = new ObjectName("jck.q", "name", "n");
        server.registerMBean(a5, na);
        server.registerMBean(b6, nb);
        server.registerMBean(new QNamed(), nn);
        Set<ObjectName> ea = qnames(na);
        Set<ObjectName> eb = qnames(nb);
        Set<ObjectName> eab = qnames(na, nb);
        Set<ObjectName> en = qnames(nn);
        Set<ObjectName> empty = new HashSet<ObjectName>();

        check(server.queryNames(null, Query.eq(Query.attr("Count"), Query.value(5))).equals(ea),
                "q eq Count==5 hits only a");
        check(server.queryNames(null, Query.eq(Query.attr("Count"), Query.value(6))).equals(eb),
                "q eq Count==6 hits only b");
        check(server.queryNames(null, Query.gt(Query.attr("Count"), Query.value(5))).equals(eb), "q gt 5 hits b");
        check(server.queryNames(null, Query.geq(Query.attr("Count"), Query.value(5))).equals(eab), "q ge 5 hits a+b");
        check(server.queryNames(null, Query.lt(Query.attr("Count"), Query.value(5))).equals(empty), "q lt 5 hits none");
        check(server.queryNames(null, Query.leq(Query.attr("Count"), Query.value(5))).equals(ea), "q le 5 hits a");
        check(server.queryNames(null, Query.eq(Query.attr("count"), Query.value(5))).equals(empty),
                "q attribute names are case sensitive");
        check(server.queryNames(null, Query.and(
                Query.gt(Query.attr("Count"), Query.value(4)),
                Query.lt(Query.attr("Count"), Query.value(6)))).equals(ea), "q and combination");
        check(server.queryNames(null, Query.or(
                Query.eq(Query.attr("Count"), Query.value(5)),
                Query.eq(Query.attr("Count"), Query.value(6)))).equals(eab), "q or combination");
        check(server.queryNames(null, Query.not(Query.eq(Query.attr("Count"), Query.value(5)))).equals(eb),
                "q not: eq-false bean kept, others silently excluded");
        check(server.queryNames(null, Query.between(Query.attr("Count"), Query.value(5), Query.value(6))).equals(eab),
                "q between inclusive on both ends");
        check(server.queryNames(null, Query.in(Query.attr("Count"),
                new ValueExp[]{Query.value(5), Query.value(7)})).equals(ea), "q in membership");
        check(server.queryNames(null, Query.eq(Query.attr(QCounter.class.getName(), "Count"), Query.value(5))).equals(ea),
                "q class-qualified attr form");
        check(server.queryNames(null, Query.eq(Query.attr("com.jck.NoSuchClass", "Count"), Query.value(5))).equals(empty),
                "q class-qualified attr with wrong class hits nothing");
        check(server.queryNames(null, Query.isInstanceOf(Query.value(QCounter.class.getName()))).equals(eab),
                "q isInstanceOf Counter");
        check(server.queryNames(null, Query.isInstanceOf(Query.value("java.lang.String"))).equals(empty),
                "q isInstanceOf miss");
        check(server.queryNames(null, Query.eq(Query.classattr(), Query.value(QCounter.class.getName()))).equals(eab),
                "q classattr eq class name");
        check(server.queryNames(null, Query.eq(Query.classattr(), Query.value("java.lang.String"))).equals(empty),
                "q classattr miss");
        check(server.queryNames(null, Query.initialSubString(Query.attr("Name"), Query.value("alp"))).equals(en),
                "q initialSubString");
        check(server.queryNames(null, Query.anySubString(Query.attr("Name"), Query.value("lph"))).equals(en),
                "q anySubString");
        check(server.queryNames(null, Query.finalSubString(Query.attr("Name"), Query.value("pha"))).equals(en),
                "q finalSubString");
        check(server.queryNames(null, Query.initialSubString(Query.attr("Name"), Query.value("zz"))).equals(empty),
                "q initialSubString miss");
        check(server.queryNames(null, Query.match(Query.attr("Name"), Query.value("al*h?"))).equals(en),
                "q match wildcards * and ?");
        check(server.queryNames(null, Query.eq(Query.plus(Query.attr("Count"), Query.value(1)), Query.value(6))).equals(ea),
                "q plus arithmetic in filter");
        check(server.queryNames(null, Query.eq(Query.minus(Query.attr("Count"), Query.value(1)), Query.value(4))).equals(ea),
                "q minus arithmetic in filter");
        check(server.queryNames(null, Query.eq(Query.times(Query.attr("Count"), Query.value(2)), Query.value(10))).equals(ea),
                "q times arithmetic in filter");
        check(server.queryNames(null, Query.eq(Query.div(Query.attr("Count"), Query.value(2)), Query.value(3))).equals(eb),
                "q div arithmetic in filter");
        check(server.queryNames(new ObjectName("jck.q:*"), Query.eq(Query.attr("Count"), Query.value(6))).equals(eb),
                "q name-pattern scoped filtering");
        Set<ObjectInstance> mb = server.queryMBeans(null, Query.eq(Query.attr("Count"), Query.value(5)));
        check(mb.size() == 1, "q queryMBeans size");
        ObjectInstance oie = mb.iterator().next();
        check(oie.getObjectName().equals(na) && oie.getClassName().equals(QCounter.class.getName()),
                "q queryMBeans returns ObjectInstance with class");
        check(server.queryNames(null, Query.eq(Query.attr("Count"), Query.value("5"))).equals(empty),
                "q numeric attribute vs string constant: no match, no exception");
        check(server.queryNames(null, Query.eq(Query.attr("Missing"), Query.value(5))).equals(empty),
                "q unknown attribute: no match, no exception");
    }
}
