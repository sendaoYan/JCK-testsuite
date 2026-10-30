/*
 * @test
 * @summary JLS 12.3.3: a resolution error is reported as an
 *          IncompatibleClassChangeError subclass (IllegalAccessError,
 *          InstantiationError, NoSuchFieldError, NoSuchMethodError).
 * @run main ResolutionErrorHierarchy
 */

public class ResolutionErrorHierarchy {
    public static void main(String[] args) {
        if (!LinkageError.class.isAssignableFrom(IncompatibleClassChangeError.class))
            throw new AssertionError("IncompatibleClassChangeError");
        if (!IncompatibleClassChangeError.class.isAssignableFrom(IllegalAccessError.class))
            throw new AssertionError("IllegalAccessError");
        if (!IncompatibleClassChangeError.class.isAssignableFrom(InstantiationError.class))
            throw new AssertionError("InstantiationError");
        if (!IncompatibleClassChangeError.class.isAssignableFrom(NoSuchFieldError.class))
            throw new AssertionError("NoSuchFieldError");
        if (!IncompatibleClassChangeError.class.isAssignableFrom(NoSuchMethodError.class))
            throw new AssertionError("NoSuchMethodError");
        if (!LinkageError.class.isAssignableFrom(UnsatisfiedLinkError.class))
            throw new AssertionError("UnsatisfiedLinkError");
        System.out.println("PASS");
    }
}
