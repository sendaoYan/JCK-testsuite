/*
 * @test
 * @summary javax.swing.SwingContainer conformance: white-box structure + black-box behavior.
 * @run main SwingContainerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SwingContainer;

public class SwingContainerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    @SwingContainer
    static class Annotated { }

    @SwingContainer(value = false, delegate = "delegateName")
    static class AnnotatedCustom { }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SwingContainer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.annotation.Annotation"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SwingContainer a = Annotated.class.getAnnotation(SwingContainer.class);
        check(a != null, "annotation present");
        check(a.value(), "default value true");
        check("".equals(a.delegate()), "default delegate empty");

        SwingContainer c = AnnotatedCustom.class.getAnnotation(SwingContainer.class);
        check(c != null, "custom annotation present");
        check(!c.value(), "value false");
        check("delegateName".equals(c.delegate()), "delegate name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
