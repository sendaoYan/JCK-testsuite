/*
 * @test
 * @summary JLS 14.30.1: patterns come in two kinds - type patterns (named or
 *          unnamed) and record patterns (with component patterns, including the
 *          match-all pattern). A nested var type pattern takes the upward
 *          projection of the corresponding record component type.
 * @run main KindsOfPatterns
 */

import java.util.ArrayList;

record IntBox(int value) {}
record Holder(ArrayList<String> a) {}

public class KindsOfPatterns {
    public static void main(String[] args) {
        // Named type pattern.
        Object o = "hi";
        if (!(o instanceof String s) || s.length() != 2)
            throw new AssertionError("named type pattern");

        // Unnamed type pattern.
        if (!(o instanceof String _))
            throw new AssertionError("unnamed type pattern");

        // Record pattern with a nested primitive type pattern.
        Object b = new IntBox(5);
        if (!(b instanceof IntBox(int i)) || i != 5)
            throw new AssertionError("record pattern with nested type pattern");

        // Match-all pattern (declares no variable).
        if (!(b instanceof IntBox(_)))
            throw new AssertionError("match-all pattern");

        // Nested var: the type is the (upward-projected) component type.
        ArrayList<String> list = new ArrayList<>();
        list.add("x");
        Object h = new Holder(list);
        if (h instanceof Holder(var a2)) {
            ArrayList<String> typed = a2;   // var -> ArrayList<String>
            if (!"x".equals(typed.get(0)))
                throw new AssertionError("nested var type");
        } else {
            throw new AssertionError("nested var did not match");
        }

        System.out.println("PASS");
    }
}
