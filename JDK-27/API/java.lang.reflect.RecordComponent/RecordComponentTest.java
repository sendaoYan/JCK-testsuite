/*
 * @test
 * @summary java.lang.reflect.RecordComponent conformance: record component accessors.
 * @run main RecordComponentTest
 */
import java.lang.reflect.RecordComponent;

public class RecordComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    record Point(int x, int y) {}
    public static void main(String[] args) {
        RecordComponent[] rcs = Point.class.getRecordComponents();
        check(rcs.length == 2, "two components");
        check("x".equals(rcs[0].getName()) && "y".equals(rcs[1].getName()), "names");
        check(rcs[0].getType() == int.class && rcs[1].getType() == int.class, "types");
        check("x".equals(rcs[0].getAccessor().getName()), "accessor");
        check(rcs[0].getDeclaringRecord() == Point.class, "declaringRecord");
        check(rcs[0].getGenericType() == int.class, "genericType");
        check(rcs[0].getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
