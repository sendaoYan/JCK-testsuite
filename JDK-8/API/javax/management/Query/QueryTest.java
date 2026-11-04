/*
 * @test
 * @summary javax.management.Query conformance: static query/expr factories and constants.
 * @run main QueryTest
 */

import javax.management.*;
public class QueryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
