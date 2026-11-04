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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
