/*
 * @test
 * @summary JLS 17.5.3 (Java SE 8): a final field may be modified via reflection
 *          (setAccessible plus Field.set); the stored value of the field changes.
 * @run main FinalFieldReflectiveModification
 */

import java.lang.reflect.Field;

public class FinalFieldReflectiveModification {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Holder {
        final int value = 42;
    }

    public static void main(String[] args) throws Exception {
        Holder h = new Holder();
        Field f = Holder.class.getDeclaredField("value");
        f.setAccessible(true);

        check(f.getInt(h) == 42, "initial final value");
        f.setInt(h, 7);
        check(f.getInt(h) == 7, "final field modified via reflection");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
