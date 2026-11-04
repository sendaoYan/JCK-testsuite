/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup.ClassOption conformance: white-box + black-box
 *          behavior, deepened with exact enum contracts: values() universe and order,
 *          name() exact strings, fresh-array semantics (mutation not observable),
 *          valueOf round-trip/null NPE ("Name is null")/unknown IAE ("ClassOption.WEAK"),
 *          compareTo total order, singleton equals/hashCode, getDeclaringClass exact.
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

    // ---- Part 3: deepened enum contracts ----
    static void testEnumContracts() {
        // the exported universe is exactly {NESTMATE, STRONG}, in declaration order
        ClassOption[] vals = ClassOption.values();
        check(vals.length == 2, "values length locked at 2");
        check(vals[0] == ClassOption.NESTMATE && vals[1] == ClassOption.STRONG,
                "values declaration order");
        // name() exact strings
        check(ClassOption.NESTMATE.name().equals("NESTMATE"), "NESTMATE name exact");
        check(ClassOption.STRONG.name().equals("STRONG"), "STRONG name exact");
        // values() returns a fresh array each call; mutating it is not observable
        ClassOption[] v1 = ClassOption.values();
        ClassOption[] v2 = ClassOption.values();
        check(v1 != v2, "values returns fresh array");
        check(java.util.Arrays.equals(v1, v2), "values content equal");
        v1[0] = ClassOption.STRONG;
        check(ClassOption.values()[0] == ClassOption.NESTMATE,
                "mutating returned array is not observable");
        // valueOf round-trips both names
        check(ClassOption.valueOf("NESTMATE") == ClassOption.NESTMATE
                && ClassOption.valueOf("STRONG") == ClassOption.STRONG,
                "valueOf round-trip");
        // valueOf(null) -> NPE with the standard Enum message
        try {
            ClassOption.valueOf(null);
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            check("Name is null".equals(e.getMessage()), "valueOf null NPE message");
        }
        // unknown constant -> IAE naming the enum class
        try {
            ClassOption.valueOf("WEAK");
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("ClassOption.WEAK"),
                    "valueOf unknown IAE message");
        }
        // compareTo is a total order consistent with ordinal
        check(ClassOption.NESTMATE.compareTo(ClassOption.STRONG) < 0, "NESTMATE < STRONG");
        check(ClassOption.STRONG.compareTo(ClassOption.NESTMATE) > 0, "STRONG > NESTMATE");
        check(ClassOption.NESTMATE.compareTo(ClassOption.NESTMATE) == 0, "compareTo self 0");
        // equals/hashCode: enum constants are singletons
        check(ClassOption.NESTMATE.equals(ClassOption.valueOf("NESTMATE")), "equals singleton");
        check(ClassOption.NESTMATE.hashCode() == ClassOption.valueOf("NESTMATE").hashCode(),
                "hashCode stable");
        check(!ClassOption.NESTMATE.equals(ClassOption.STRONG), "distinct constants unequal");
        // declaring class is exactly Lookup.ClassOption
        check(ClassOption.NESTMATE.getDeclaringClass().getName()
                .equals("java.lang.invoke.MethodHandles$Lookup$ClassOption"),
                "getDeclaringClass exact");
        // getDeclaringClass of a values() alias is the same class
        check(ClassOption.values()[1].getDeclaringClass() == ClassOption.STRONG.getDeclaringClass(),
                "values alias declaring class");
        // Enum.valueOf generic path agrees with the generated valueOf
        check(Enum.valueOf(ClassOption.class, "STRONG") == ClassOption.STRONG,
                "Enum.valueOf agrees");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testEnumContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
