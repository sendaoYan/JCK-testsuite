/*
 * @test
 * @summary java.io.ObjectInputFilter.Config conformance: createFilter and serial filter factory.
 * @run main ConfigTest
 */
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.Config;
import java.io.ObjectInputFilter.FilterInfo;
import java.io.ObjectInputFilter.Status;

public class ConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static FilterInfo info(final Class<?> c) {
        return new FilterInfo() {
            public Class<?> serialClass() { return c; }
            public long arrayLength() { return -1L; }
            public long depth() { return 1L; }
            public long references() { return 1L; }
            public long streamBytes() { return 0L; }
        };
    }

    public static void main(String[] args) {
        ObjectInputFilter f = Config.createFilter("java.lang.*;java.util.*;!*");
        check(f != null, "createFilter non-null");
        check(f.checkInput(info(String.class)) == Status.ALLOWED, "allowed class");
        check(f.checkInput(info(java.util.List.class)) == Status.ALLOWED, "util allowed");
        check(f.checkInput(info(java.io.File.class)) == Status.REJECTED, "reject other");
        check(Config.createFilter("java.lang.String;!*") != null, "single class filter");
        check(Config.getSerialFilterFactory() != null, "getSerialFilterFactory non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
