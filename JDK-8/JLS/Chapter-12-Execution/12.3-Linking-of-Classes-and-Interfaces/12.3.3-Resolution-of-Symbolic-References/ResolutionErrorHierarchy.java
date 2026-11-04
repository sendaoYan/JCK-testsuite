/*
 * @test
 * @summary JLS 12.3.3 (Java SE 8): a resolution error is reported as an
 *          IncompatibleClassChangeError subclass (IllegalAccessError,
 *          InstantiationError, NoSuchFieldError, NoSuchMethodError).
 * @run main ResolutionErrorHierarchy
 */

public class ResolutionErrorHierarchy {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(LinkageError.class.isAssignableFrom(IncompatibleClassChangeError.class), "IncompatibleClassChangeError");
        check(IncompatibleClassChangeError.class.isAssignableFrom(IllegalAccessError.class), "IllegalAccessError");
        check(IncompatibleClassChangeError.class.isAssignableFrom(InstantiationError.class), "InstantiationError");
        check(IncompatibleClassChangeError.class.isAssignableFrom(NoSuchFieldError.class), "NoSuchFieldError");
        check(IncompatibleClassChangeError.class.isAssignableFrom(NoSuchMethodError.class), "NoSuchMethodError");
        check(LinkageError.class.isAssignableFrom(UnsatisfiedLinkError.class), "UnsatisfiedLinkError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
