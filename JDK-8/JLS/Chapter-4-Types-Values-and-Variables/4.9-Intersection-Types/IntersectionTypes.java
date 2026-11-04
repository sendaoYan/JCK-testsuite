/*
 * @test
 * @summary JLS 4.9 (Java SE 8): an intersection type takes the form T1 & ... &
 *          Tn and arises from a type-variable bound. The values of an
 *          intersection type are objects that are values of all of the types.
 *          Members of an intersection type are the members of all its types.
 * @run main IntersectionTypes
 */

import java.io.Serializable;

class Base {
    public String base() { return "base"; }
}

interface Labeled {
    String label();
}

public class IntersectionTypes {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // T is an intersection type Base & Labeled.
    static <T extends Base & Labeled> String describe(T t) {
        // Members of both Base and Labeled are accessible.
        return t.base() + ":" + t.label();
    }

    static class Both extends Base implements Labeled {
        public String label() { return "L"; }
    }

    public static void main(String[] args) {
        check(describe(new Both()).equals("base:L"),
              "intersection type members come from all constituent types");

        // A type variable bound to an intersection may be replaced by a subtype
        // of every constituent type.
        Both both = new Both();
        check(describe(both).equals("base:L"), "subtype of every constituent satisfies intersection");

        // Intersection with a marker interface (Serializable) is allowed.
        check(serializable(Integer.valueOf(1)), "intersection bound with marker interface");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static <T extends Number & Serializable> boolean serializable(T t) {
        return t != null;
    }
}
