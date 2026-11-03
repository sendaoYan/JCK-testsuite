/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup.ClassOption conformance: white-box + black-box behavior.
 * @run main ClassOptionTest
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup.ClassOption;
import java.lang.reflect.Modifier;

public class ClassOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** nested class whose bytes are re-defined as a hidden class */
    public static class Hidden {
        public static int magic() { return 4711; }
    }

    static byte[] classBytes() throws Exception {
        try (java.io.InputStream in =
                     ClassOptionTest.class.getResourceAsStream("ClassOptionTest$Hidden.class")) {
            check(in != null, "nested class bytes available");
            return in.readAllBytes();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 2, "two constants");
        check(c.getDeclaringClass() == Class.forName("java.lang.invoke.MethodHandles$Lookup"),
              "nested in Lookup");
        check(Modifier.isFinal(c.getModifiers()), "final enum");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        // constants, order, mechanics
        check(ClassOption.values()[0] == ClassOption.NESTMATE, "NESTMATE first");
        check(ClassOption.values()[1] == ClassOption.STRONG, "STRONG second");
        check(ClassOption.valueOf("NESTMATE") == ClassOption.NESTMATE, "valueOf NESTMATE");
        check(ClassOption.valueOf("STRONG") == ClassOption.STRONG, "valueOf STRONG");
        try { ClassOption.valueOf("WEAK"); check(false, "unknown rejected"); }
        catch (IllegalArgumentException expected) { check(true, "valueOf unknown IAE"); }
        check(ClassOption.NESTMATE.compareTo(ClassOption.STRONG) < 0, "compareTo");
        check(ClassOption.NESTMATE.ordinal() == 0 && ClassOption.STRONG.ordinal() == 1,
              "ordinals");

        // NESTMATE: hidden class can access private members of the nest host
        byte[] bytes = classBytes();
        MethodHandles.Lookup nestmate = MethodHandles.lookup()
                .defineHiddenClass(bytes, false, ClassOption.NESTMATE);
        check(nestmate.lookupClass().isHidden(), "defineHiddenClass yields hidden class");
        check(nestmate.hasPrivateAccess(), "NESTMATE grants private access");
        check((int) nestmate.findStatic(nestmate.lookupClass(), "magic",
                java.lang.invoke.MethodType.methodType(int.class)).invokeExact() == 4711,
              "hidden class method invocable");

        // STRONG: hidden class is strongly reachable (no observable weak behavior, but accepted)
        MethodHandles.Lookup strong = MethodHandles.lookup()
                .defineHiddenClass(bytes, false, ClassOption.STRONG);
        check(strong.lookupClass().isHidden(), "STRONG accepted");
        check(!strong.lookupClass().getName().equals(nestmate.lookupClass().getName()),
              "hidden classes get distinct identities");

        // both options together
        MethodHandles.Lookup both = MethodHandles.lookup()
                .defineHiddenClass(bytes, false, ClassOption.NESTMATE, ClassOption.STRONG);
        check(both.lookupClass().isHidden(), "NESTMATE+STRONG accepted");
        // hidden class names carry the /0x... suffix
        check(both.lookupClass().getName().contains("/"), "hidden class name has slash form");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
