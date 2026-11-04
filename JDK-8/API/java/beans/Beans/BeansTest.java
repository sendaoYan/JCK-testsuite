/*
 * @test
 * @summary java.beans.Beans conformance: isInstanceOf/getInstanceOf.
 * @run main BeansTest
 */

import java.beans.*;
public class BeansTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Beans.isInstanceOf("x", String.class), "isInstanceOf String");
        check(!Beans.isInstanceOf("x", Integer.class), "isInstanceOf Integer false");
        check("x".equals(Beans.getInstanceOf("x", String.class)), "getInstanceOf");
        check(!Beans.isDesignTime(), "not design time");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
