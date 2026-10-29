/*
 * @test
 * @summary JLS 7.1: members of different packages may have the same simple name;
 *          they are distinct, reflected in their different fully qualified names.
 * @compile a/Thing.java b/Thing.java Main.java
 * @run main Main
 */

public class Main {
    public static void main(String[] args) {
        a.Thing ta = new a.Thing();
        b.Thing tb = new b.Thing();
        if (ta.id != 1) throw new AssertionError("a.Thing");
        if (tb.id != 2) throw new AssertionError("b.Thing");

        // 7.1: distinct package members have distinct fully qualified names.
        String fa = ta.getClass().getName();
        String fb = tb.getClass().getName();
        if (!fa.equals("a.Thing")) throw new AssertionError("a.Thing FQN: " + fa);
        if (!fb.equals("b.Thing")) throw new AssertionError("b.Thing FQN: " + fb);
        if (fa.equals(fb)) throw new AssertionError("distinct types");

        System.out.println("PASS");
    }
}
