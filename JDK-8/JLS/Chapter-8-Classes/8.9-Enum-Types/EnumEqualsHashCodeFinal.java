/*
 * @test
 * @summary JLS 8.9.3 (Java SE 8): the equals, hashCode and clone methods inherited
 *          from java.lang.Enum are final, so enum constants have identity semantics.
 * @run main EnumEqualsHashCodeFinal
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EnumEqualsHashCodeFinal {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum E { A, B }

    public static void main(String[] args) throws Exception {
        Method eq = Enum.class.getMethod("equals", Object.class);
        Method hc = Enum.class.getMethod("hashCode");
        Method cl = Enum.class.getDeclaredMethod("clone");

        check(Modifier.isFinal(eq.getModifiers()), "equals is final");
        check(Modifier.isFinal(hc.getModifiers()), "hashCode is final");
        check(Modifier.isFinal(cl.getModifiers()), "clone is final");
        check(E.A.equals(E.A), "equals is identity-based for same constant");
        check(!E.A.equals(E.B), "equals distinguishes distinct constants");
        check(E.A.hashCode() == E.A.hashCode(), "hashCode is stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
