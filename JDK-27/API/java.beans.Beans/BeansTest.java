/*
 * @test
 * @summary java.beans.Beans conformance: design time, instantiate and instance checks.
 * @run main BeansTest
 */

import java.beans.*;

public class BeansTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(Beans.isDesignTime() == false, "design time default false");
        Beans.setDesignTime(true);
        check(Beans.isDesignTime() == true, "design time true");
        Beans.setDesignTime(false);
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        check(Beans.isGuiAvailable() == !headless, "gui available matches headless");
        Object o = Beans.instantiate(null, "java.util.ArrayList");
        check(o instanceof java.util.ArrayList, "instantiate ArrayList");
        check(Beans.isInstanceOf(o, java.util.List.class), "isInstanceOf");
        check(Beans.getInstanceOf(o, java.util.List.class) == o, "getInstanceOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
