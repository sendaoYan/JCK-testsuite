/*
 * @test
 * @summary JLS 9.6/9.7 (Java SE 8): annotation types declare elements with
 *          defaults; retention and target control availability, and inherited
 *          annotations propagate to subclasses.
 * @compile Mark.java
 * @run main AnnotationTest
 */

public class AnnotationTest {
    public static void main(String[] args) {
        Mark m = AnnotatedBase.class.getAnnotation(Mark.class);
        if (m == null) throw new AssertionError("annotation present");
        if (!m.value().equals("dflt") || m.num() != 7) throw new AssertionError("annotation defaults");

        Mark ms = AnnotatedSub.class.getAnnotation(Mark.class);
        if (ms == null) throw new AssertionError("@Inherited propagated to subclass");

        Mark e = ExplicitMark.class.getAnnotation(Mark.class);
        if (e == null || !e.value().equals("x") || e.num() != 9) throw new AssertionError("explicit values");

        System.out.println("PASS");
    }
}
