/*
 * @test
 * @summary java.lang.Record conformance: abstract class with equals/hashCode/toString.
 * @run main RecordTest
 */
import java.lang.Record;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(Modifier.isAbstract(Record.class.getModifiers()), "abstract");
        check(!Record.class.isInterface(), "not interface");
        check(Record.class.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isProtected(Record.class.getDeclaredConstructor().getModifiers()), "protected ctor");
        boolean hasEq = false, hasHash = false, hasStr = false;
        for (Method m : Record.class.getDeclaredMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                if (m.getName().equals("equals")) hasEq = true;
                if (m.getName().equals("hashCode")) hasHash = true;
                if (m.getName().equals("toString")) hasStr = true;
            }
        }
        check(hasEq, "abstract equals");
        check(hasHash, "abstract hashCode");
        check(hasStr, "abstract toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
