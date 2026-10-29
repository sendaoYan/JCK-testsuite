/*
 * @test
 * @summary JLS 6.4.1 Example 6.4.1-2: a type declaration named Vector shadows
 *          java.util.Vector imported on demand; the JDK type stays reachable
 *          through a fully qualified name.
 * @run main TypeShadowing
 */

import java.util.*;

class Vector {
    int[] val = { 1, 2 };
}

public class TypeShadowing {
    public static void main(String[] args) {
        Vector v = new Vector();            // the local Vector, not java.util.Vector
        if (v.val[0] != 1) throw new AssertionError("local Vector used");

        java.util.Vector<String> jv = new java.util.Vector<>();
        if (jv.size() != 0) throw new AssertionError("java.util.Vector accessible");

        System.out.println("PASS");
    }
}
