/*
 * @test
 * @summary java.beans.JavaBean conformance: white-box structure + black-box behavior.
 * @run main JavaBeanTest
 */

import java.beans.JavaBean;

public class JavaBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @JavaBean(description = "desc", defaultProperty = "foo", defaultEventSet = "evt")
    public static class Annotated {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.JavaBean");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("description").getReturnType() == String.class, "description returns String");
    }

    static void testBlackBox() throws Exception {
        JavaBean jb = Annotated.class.getAnnotation(JavaBean.class);
        check(jb != null, "annotation present");
        check("desc".equals(jb.description()), "description");
        check("foo".equals(jb.defaultProperty()), "defaultProperty");
        check("evt".equals(jb.defaultEventSet()), "defaultEventSet");

        // defaults
        JavaBean def = java.beans.JavaBean.class.getAnnotation(JavaBean.class);
        check(def == null, "JavaBean itself not annotated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
