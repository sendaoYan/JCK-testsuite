/*
 * @test
 * @summary java.io.Serial conformance: annotation type with METHOD and FIELD targets.
 * @run main SerialTest
 */
import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public class SerialTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Serial.class.isAnnotation(), "is annotation");
        check(Annotation.class.isAssignableFrom(Serial.class), "extends Annotation");
        Target t = Serial.class.getAnnotation(Target.class);
        check(t != null, "has Target");
        boolean hasMethod = false, hasField = false;
        for (ElementType e : t.value()) {
            if (e == ElementType.METHOD) hasMethod = true;
            if (e == ElementType.FIELD) hasField = true;
        }
        check(hasMethod, "targets METHOD");
        check(hasField, "targets FIELD");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
