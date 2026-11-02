/*
 * @test
 * @summary java.io.ObjectInputFilter.FilterInfo conformance: interface and accessor methods.
 * @run main FilterInfoTest
 */
import java.io.ObjectInputFilter.FilterInfo;

public class FilterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(FilterInfo.class.isInterface(), "is interface");
        FilterInfo info = new FilterInfo() {
            public Class<?> serialClass() { return String.class; }
            public long arrayLength() { return 3L; }
            public long depth() { return 2L; }
            public long references() { return 4L; }
            public long streamBytes() { return 5L; }
        };
        check(info.serialClass() == String.class, "serialClass");
        check(info.arrayLength() == 3L, "arrayLength");
        check(info.depth() == 2L, "depth");
        check(info.references() == 4L, "references");
        check(info.streamBytes() == 5L, "streamBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
