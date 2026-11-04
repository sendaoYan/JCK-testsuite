/*
 * @test
 * @summary javax.management.openmbean.SimpleType conformance: isValue and constants.
 * @run main SimpleTypeTest
 */

import javax.management.openmbean.*;
public class SimpleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(SimpleType.STRING.isValue("x"), "STRING value");
        check(!SimpleType.STRING.isValue(Integer.valueOf(1)), "STRING rejects integer");
        check(SimpleType.INTEGER.isValue(Integer.valueOf(5)), "INTEGER value");
        check(SimpleType.INTEGER.equals(SimpleType.INTEGER), "equals");
        check(!SimpleType.INTEGER.equals(SimpleType.STRING), "not equals different type");
        check(SimpleType.VOID != null && SimpleType.OBJECTNAME != null, "constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
