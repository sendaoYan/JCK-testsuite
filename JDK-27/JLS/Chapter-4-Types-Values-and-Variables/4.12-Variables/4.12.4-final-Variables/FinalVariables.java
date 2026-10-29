/*
 * @test
 * @summary JLS 4.12.4: a final variable may only be assigned to once. A final
 *          variable holding a reference still allows the referenced object's
 *          state to change, but the variable always refers to the same object.
 *          Some variables are implicitly final (interface fields, resources of
 *          try-with-resources, multi-catch exception parameters).
 * @run main FinalVariables
 */

class Mutable {
    int x;
}

public class FinalVariables {
    public static void main(String[] args) {
        // A final variable is assigned exactly once.
        final int one = 1;
        if (one != 1) throw new AssertionError("final int");

        // A blank final: declared without initializer, assigned once later.
        final int blank;
        blank = 42;
        if (blank != 42) throw new AssertionError("blank final");

        // A final reference may still refer to a mutable object.
        final Mutable m = new Mutable();
        m.x = 1;                  // state may change
        m.x = 2;
        if (m.x != 2) throw new AssertionError("final reference's object state is mutable");
        // (The variable m always refers to the same object.)

        // A final array reference allows component mutation.
        final int[] a = new int[1];
        a[0] = 7;
        if (a[0] != 7) throw new AssertionError("final array components are mutable");

        // A constant variable is a final primitive or String with a constant
        // expression initializer.
        final String constant = "abc";
        if (!constant.equals("abc")) throw new AssertionError("constant variable");

        // Effectively final: not declared final, never assigned after init.
        int effectivelyFinal = 10;
        Runnable r = () -> {
            if (effectivelyFinal != 10) throw new AssertionError("effectively final");
        };
        r.run();

        // Implicitly final: interface field, try-with-resources resource,
        // multi-catch exception parameter.
        if (!Iface.CONST.equals("const")) throw new AssertionError("implicit final interface field");

        System.out.println("PASS");
    }

    interface Iface {
        // Interface fields are implicitly public static final.
        String CONST = "const";
    }
}
