/*
 * @test
 * @summary java.beans.Beans conformance: white-box structure + black-box behavior.
 * @run main BeansTest
 */

import java.beans.Beans;

public class BeansTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Beans");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        Beans.setDesignTime(true);
        check(Beans.isDesignTime(), "isDesignTime true");
        Beans.setDesignTime(false);
        check(!Beans.isDesignTime(), "isDesignTime false");

        Beans.setGuiAvailable(true);
        check(Beans.isGuiAvailable(), "isGuiAvailable true");
        Beans.setGuiAvailable(false);
        check(!Beans.isGuiAvailable(), "isGuiAvailable false");

        check(Beans.isInstanceOf("x", String.class), "isInstanceOf true");
        check(!Beans.isInstanceOf("x", Integer.class), "isInstanceOf false");
        check(Beans.isInstanceOf("x", CharSequence.class), "isInstanceOf interface");
        check("x".equals(Beans.getInstanceOf("x", String.class)), "getInstanceOf same");
        check("x".equals(Beans.getInstanceOf("x", CharSequence.class)), "getInstanceOf interface");

        Object o = Beans.instantiate(Thread.currentThread().getContextClassLoader(), "java.lang.String");
        check(o instanceof String, "instantiate by name");
        Object o2 = Beans.instantiate(Thread.currentThread().getContextClassLoader(), "java.lang.String", null);
        check(o2 instanceof String, "instantiate by name with BeanContext");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
