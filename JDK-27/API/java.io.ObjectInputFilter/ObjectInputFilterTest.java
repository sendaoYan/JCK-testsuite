/*
 * @test
 * @summary java.io.ObjectInputFilter conformance: interface, allowFilter/rejectFilter/merge/rejectUndecidedClass static filters.
 * @run main ObjectInputFilterTest
 */
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.FilterInfo;
import java.io.ObjectInputFilter.Status;

public class ObjectInputFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static FilterInfo info(final Class<?> c, final long arrayLen) {
        return new FilterInfo() {
            public Class<?> serialClass() { return c; }
            public long arrayLength() { return arrayLen; }
            public long depth() { return 1L; }
            public long references() { return 1L; }
            public long streamBytes() { return 0L; }
        };
    }

    public static void main(String[] args) {
        check(ObjectInputFilter.class.isInterface(), "is interface");
        ObjectInputFilter allow = ObjectInputFilter.allowFilter(c -> c == String.class, Status.REJECTED);
        check(allow != null, "allowFilter non-null");
        check(allow.checkInput(info(String.class, -1L)) == Status.ALLOWED, "allowFilter match ALLOWED");
        check(allow.checkInput(info(Integer.class, -1L)) == Status.REJECTED, "allowFilter miss otherStatus");
        ObjectInputFilter reject = ObjectInputFilter.rejectFilter(c -> c == String.class, Status.ALLOWED);
        check(reject.checkInput(info(String.class, -1L)) == Status.REJECTED, "rejectFilter match REJECTED");
        check(reject.checkInput(info(Integer.class, -1L)) == Status.ALLOWED, "rejectFilter miss otherStatus");
        check(ObjectInputFilter.merge(allow, reject) != null, "merge non-null");
        check(ObjectInputFilter.rejectUndecidedClass(allow) != null, "rejectUndecidedClass non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
