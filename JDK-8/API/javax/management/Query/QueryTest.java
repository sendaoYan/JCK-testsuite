/*
 * @test
 * @summary javax.management.Query conformance: full contract coverage.
 *          every static factory returns non-null QueryExp/ValueExp, operator
 *          constants, numeric/string/boolean comparisons via apply(),
 *          arithmetic ops incl. BadStringOperationException for non-PLUS string
 *          op, and attr/value/classattr/in/isInstanceOf factories.
 * @run main QueryTest
 */

import javax.management.AttributeValueExp;
import javax.management.BadStringOperationException;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.StringValueExp;
import javax.management.ValueExp;
import javax.management.*;
import java.util.*;

public class QueryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        check(Query.gt(Query.value(1), Query.value(0)) != null, "gt");
        check(Query.eq(Query.value(1), Query.value(1)) != null, "eq");
        check(Query.and(Query.gt(Query.value(2), Query.value(1)), Query.gt(Query.value(3), Query.value(1))) != null, "and");
        check(Query.or(Query.gt(Query.value(1), Query.value(0)), Query.gt(Query.value(0), Query.value(1))) != null, "or");
        check(Query.not(Query.gt(Query.value(1), Query.value(0))) != null, "not");
        check(Query.attr("Count") instanceof AttributeValueExp, "attr");
        check(Query.value("x") instanceof StringValueExp, "value(String)");
        check(Query.plus(Query.value(1), Query.value(2)) != null, "plus");
        check(Query.times(Query.value(2), Query.value(3)) != null, "times");
        check(Query.initialSubString(Query.attr("a"), Query.value("x")) != null, "initialSubString");
        check(Query.GT != Query.EQ && Query.PLUS != Query.DIV, "constants distinct");
    }

    static void testAllFactories() {
        check(Query.gt(Query.value(1), Query.value(0)) instanceof QueryExp, "gt");
        check(Query.geq(Query.value(1), Query.value(1)) instanceof QueryExp, "geq");
        check(Query.lt(Query.value(0), Query.value(1)) instanceof QueryExp, "lt");
        check(Query.leq(Query.value(1), Query.value(1)) instanceof QueryExp, "leq");
        check(Query.eq(Query.value(1), Query.value(1)) instanceof QueryExp, "eq");
        check(Query.between(Query.value(1), Query.value(0), Query.value(2)) instanceof QueryExp, "between");
        check(Query.match(Query.attr("a"), Query.value("x*")) instanceof QueryExp, "match");
        check(Query.in(Query.value(1), new ValueExp[]{Query.value(0), Query.value(1)}) instanceof QueryExp, "in");
        check(Query.initialSubString(Query.attr("a"), Query.value("x")) instanceof QueryExp, "initialSubString");
        check(Query.anySubString(Query.attr("a"), Query.value("x")) instanceof QueryExp, "anySubString");
        check(Query.finalSubString(Query.attr("a"), Query.value("x")) instanceof QueryExp, "finalSubString");
        check(Query.isInstanceOf(Query.value("java.lang.String")) instanceof QueryExp, "isInstanceOf");

        check(Query.attr("a", "b") instanceof AttributeValueExp, "attr(className,name)");
        check(Query.classattr() instanceof AttributeValueExp, "classattr");
        check(Query.value((Number) Integer.valueOf(1)) instanceof ValueExp, "value(Number)");
        check(Query.value(1) instanceof ValueExp, "value(int)");
        check(Query.value(1L) instanceof ValueExp, "value(long)");
        check(Query.value(1.5f) instanceof ValueExp, "value(float)");
        check(Query.value(1.5d) instanceof ValueExp, "value(double)");
        check(Query.value(true) instanceof ValueExp, "value(boolean)");
        check(Query.minus(Query.value(1), Query.value(2)) instanceof ValueExp, "minus");
        check(Query.div(Query.value(1), Query.value(2)) instanceof ValueExp, "div");
    }

    static void testOperatorConstants() {
        check(Query.GT == 0 && Query.LT == 1 && Query.GE == 2 && Query.LE == 3 && Query.EQ == 4, "rel op constants");
        check(Query.PLUS == 0 && Query.MINUS == 1 && Query.TIMES == 2 && Query.DIV == 3, "arith op constants");
    }

    static void testApplyNumericBooleanString() throws Exception {
        check(Query.gt(Query.value(2), Query.value(1)).apply(null), "gt apply");
        check(!Query.gt(Query.value(1), Query.value(2)).apply(null), "gt apply false");
        check(Query.eq(Query.value(2), Query.value(2)).apply(null), "eq apply");
        check(Query.eq(Query.value("a"), Query.value("a")).apply(null), "eq string apply");
        check(Query.lt(Query.value("a"), Query.value("b")).apply(null), "string lt apply");
        check(Query.eq(Query.value(true), Query.value(true)).apply(null), "boolean eq apply");
    }

    static void testArithmeticApply() throws Exception {
        ValueExp sum = Query.plus(Query.value(2), Query.value(3)).apply(null);
        check("5".equals(sum.toString()), "plus numeric apply");
        ValueExp diff = Query.minus(Query.value(5), Query.value(2)).apply(null);
        check("3".equals(diff.toString()), "minus numeric apply");
        StringValueExp concat = (StringValueExp) Query.plus(Query.value("a"), Query.value("b")).apply(null);
        check("ab".equals(concat.getValue()), "plus string concat");

        checkThrows(BadStringOperationException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception { Query.minus(Query.value("a"), Query.value("b")).apply(null); }
                }, "minus string op -> BadStringOperationException");
        checkThrows(BadStringOperationException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception { Query.div(Query.value("a"), Query.value("b")).apply(null); }
                }, "div string op -> BadStringOperationException");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testAllFactories();
        testOperatorConstants();
        testApplyNumericBooleanString();
        testArithmeticApply();
        testQueryFiltering();
        System.out.println("PASS (" + passed + " assertions)");
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
