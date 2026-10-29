/*
 * @test
 * @summary JLS 4.12.3: there are eight kinds of variables: class variables,
 *          instance variables, array components, method parameters,
 *          constructor parameters, lambda parameters, exception parameters,
 *          and local variables (including pattern variables).
 * @run main KindsOfVariables
 */

import java.util.function.IntUnaryOperator;

class Holder {
    static int classVar;          // (1) class variable
    int instanceVar;              // (2) instance variable
    int[] w = new int[1];         // (3) array component w[0]

    Holder(int init) {            // (5) constructor parameter
        instanceVar = init;
    }

    int setX(int x) {             // (4) method parameter
        int oldx = this.instanceVar;   // (8) local variable
        this.instanceVar = x;
        return oldx;
    }

    boolean isPositive() {
        int v = this.instanceVar;
        return v > 0;
    }
}

public class KindsOfVariables {
    public static void main(String[] args) {
        Holder.classVar = 1;                       // class variable
        if (Holder.classVar != 1) throw new AssertionError("class variable");

        Holder h = new Holder(10);                 // constructor parameter
        if (h.instanceVar != 10) throw new AssertionError("instance variable");

        if (h.w[0] != 0) throw new AssertionError("array component default");

        if (h.setX(20) != 10) throw new AssertionError("method parameter + local variable");
        if (h.instanceVar != 20) throw new AssertionError("local variable assignment");

        // (6) lambda parameter.
        IntUnaryOperator f = (int n) -> n * 2;
        if (f.applyAsInt(3) != 6) throw new AssertionError("lambda parameter");

        // (7) exception parameter.
        try {
            throw new IllegalArgumentException("boom");
        } catch (IllegalArgumentException e) {   // exception parameter
            if (e == null || !e.getMessage().equals("boom")) {
                throw new AssertionError("exception parameter");
            }
        }

        // (8) pattern variable (a local variable declared by a pattern).
        Object o = new Holder(5);
        if (o instanceof Holder hh) {             // pattern variable
            if (hh.instanceVar != 5) throw new AssertionError("pattern variable");
        }

        System.out.println("PASS");
    }
}
