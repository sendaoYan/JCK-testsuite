/*
 * @test
 * @summary JLS 12.2.1 (Java SE 8): a loading error is reported as a subclass of
 *          LinkageError (ClassCircularityError, ClassFormatError,
 *          NoClassDefFoundError).
 * @run main LoadingErrorHierarchy
 */

public class LoadingErrorHierarchy {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(LinkageError.class.isAssignableFrom(ClassCircularityError.class), "ClassCircularityError");
        check(LinkageError.class.isAssignableFrom(ClassFormatError.class), "ClassFormatError");
        check(LinkageError.class.isAssignableFrom(NoClassDefFoundError.class), "NoClassDefFoundError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
