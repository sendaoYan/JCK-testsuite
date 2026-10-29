/*
 * @test
 * @summary JLS 4.5.1: type arguments may be reference types or wildcards. The
 *          unbounded wildcard ? is equivalent to ? extends Object; ? extends B
 *          is an upper bound and ? super B is a lower bound.
 * @run main TypeArgumentsOfParameterizedTypes
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TypeArgumentsOfParameterizedTypes {
    // A wildcard collection accepts any kind of collection (Example 4.5.1-1).
    static int printCollection(Collection<?> c) {
        int count = 0;
        for (Object o : c) count++;
        return count;
    }

    // ? extends E: an upper-bounded wildcard.
    static int sum(Collection<? extends Number> c) {
        int s = 0;
        for (Number n : c) s += n.intValue();
        return s;
    }

    // ? super T: a lower-bounded wildcard.
    static void addTo(List<? super Integer> list) {
        list.add(1);
    }

    public static void main(String[] args) {
        Collection<String> cs = new ArrayList<String>();
        cs.add("hello");
        cs.add("world");
        if (printCollection(cs) != 2) throw new AssertionError("unbounded wildcard");

        // The unbounded wildcard ? is equivalent to ? extends Object.
        List<?> u1 = new ArrayList<String>();
        List<? extends Object> u2 = new ArrayList<String>();
        if (printCollection(u1) != 0 || printCollection(u2) != 0) {
            throw new AssertionError("? equals ? extends Object");
        }

        // Upper-bounded wildcard accepts any Number subtype.
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1); ints.add(2); ints.add(3);
        if (sum(ints) != 6) throw new AssertionError("? extends Number");

        // Lower-bounded wildcard: only Integer (and its supertypes) may be added.
        List<Number> nums = new ArrayList<Number>();
        addTo(nums);
        if (nums.size() != 1 || nums.get(0) != Integer.valueOf(1)) {
            throw new AssertionError("? super Integer");
        }

        System.out.println("PASS");
    }
}
