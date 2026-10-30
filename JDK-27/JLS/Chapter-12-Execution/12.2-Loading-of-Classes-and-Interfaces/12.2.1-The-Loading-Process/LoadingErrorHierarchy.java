/*
 * @test
 * @summary JLS 12.2.1: a loading error is reported as a subclass of LinkageError
 *          (ClassCircularityError, ClassFormatError, NoClassDefFoundError).
 * @run main LoadingErrorHierarchy
 */

public class LoadingErrorHierarchy {
    public static void main(String[] args) {
        if (!LinkageError.class.isAssignableFrom(ClassCircularityError.class))
            throw new AssertionError("ClassCircularityError");
        if (!LinkageError.class.isAssignableFrom(ClassFormatError.class))
            throw new AssertionError("ClassFormatError");
        if (!LinkageError.class.isAssignableFrom(NoClassDefFoundError.class))
            throw new AssertionError("NoClassDefFoundError");
        System.out.println("PASS");
    }
}
