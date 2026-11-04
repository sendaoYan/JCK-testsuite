/*
 * @test
 * @summary JLS 8.9: an enum class is implicitly final and its direct superclass is
 *          java.lang.Enum<E>.
 * @run main EnumIsImplicitlyFinal
 */

import java.lang.reflect.Modifier;

public class EnumIsImplicitlyFinal {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum E { A }

    public static void main(String[] args) {
        check(Modifier.isFinal(E.class.getModifiers()), "enum is implicitly final");
        check(Enum.class.isAssignableFrom(E.class), "enum's direct superclass is Enum");
        check(E.class.getSuperclass() == Enum.class, "direct superclass is exactly java.lang.Enum");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
