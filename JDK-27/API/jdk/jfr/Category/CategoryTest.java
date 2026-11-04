/*
 * @test
 * @summary jdk.jfr.Category conformance: white-box structure + black-box annotation contract.
 * @run main CategoryTest
 */

import java.lang.annotation.*;
import java.lang.reflect.*;
import jdk.jfr.Category;

public class CategoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    @Category({"a","b"})
    static class Holder {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Category");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "isInterface");
        check(c.isAnnotation(), "isAnnotation");
        check(Annotation.class.isAssignableFrom(c), "implements Annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "one interface");
        Retention r = c.getAnnotation(Retention.class);
        check(r != null && r.value() == RetentionPolicy.RUNTIME, "runtime retention");
        Target t = c.getAnnotation(Target.class);
        check(t != null, "has target");
        Method m = c.getDeclaredMethod("value");
        check(m.getReturnType() == String[].class, "value element type");
    }

    static void testBlackBox() throws Exception {
        Category a = Holder.class.getAnnotation(Category.class);
        check(a != null, "annotation applied");
        check(a.value().length == 2, "value round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
