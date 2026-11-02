/*
 * @test
 * @summary java.lang.LiveStackFrame.PrimitiveSlot conformance: abstract nested class reflection.
 * @run main PrimitiveSlotTest
 */
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PrimitiveSlotTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.lang.LiveStackFrame$PrimitiveSlot");
        check(c != null, "loadable");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        boolean hasSize = false, hasInt = false, hasLong = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("size")) hasSize = true;
            if (m.getName().equals("intValue")) hasInt = true;
            if (m.getName().equals("longValue")) hasLong = true;
        }
        check(hasSize, "size()");
        check(hasInt, "intValue()");
        check(hasLong, "longValue()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
