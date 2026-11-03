/*
 * @test
 * @summary java.beans.BeanProperty conformance: white-box structure + black-box behavior.
 * @run main BeanPropertyTest
 */

import java.beans.BeanProperty;

public class BeanPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        @BeanProperty
        public int getFoo() { return 1; }
        @BeanProperty(bound = false, expert = true, description = "d")
        public int getBar() { return 2; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanProperty");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("bound").getReturnType() == boolean.class, "bound returns boolean");
    }

    static void testBlackBox() throws Exception {
        BeanProperty foo = Bean.class.getMethod("getFoo").getAnnotation(BeanProperty.class);
        check(foo != null, "foo annotation present");
        check(foo.bound(), "bound default true");
        check(!foo.expert() && !foo.hidden() && !foo.preferred() && !foo.required() && !foo.visualUpdate(), "other bool defaults false");
        check("".equals(foo.description()), "description default empty");
        check(foo.enumerationValues().length == 0, "enumerationValues default empty");

        BeanProperty bar = Bean.class.getMethod("getBar").getAnnotation(BeanProperty.class);
        check(!bar.bound(), "bound false");
        check(bar.expert(), "expert true");
        check("d".equals(bar.description()), "description d");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
