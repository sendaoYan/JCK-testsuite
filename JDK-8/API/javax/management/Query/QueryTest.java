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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
