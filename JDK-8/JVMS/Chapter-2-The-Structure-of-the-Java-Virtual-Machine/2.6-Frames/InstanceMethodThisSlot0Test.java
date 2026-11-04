/*
 * @test
 * @summary JVMS 2.6.1: the local variable array of an instance method holds the
 *          reference `this` in slot 0, so the first explicit argument is in
 *          slot 1 and is read with iload_1 while `this` is read with aload_0.
 * @compile InstanceMethodThisSlot0.jasm
 * @run main InstanceMethodThisSlot0Test
 */

public class InstanceMethodThisSlot0Test {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("InstanceMethodThisSlot0");
        Object inst = c.getDeclaredConstructor(int.class).newInstance(40);
        java.lang.reflect.Method add = c.getMethod("add", int.class);

        check(add.invoke(inst, 2).equals(42),
                "`this` (slot 0) and argument (slot 1) combine correctly");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
