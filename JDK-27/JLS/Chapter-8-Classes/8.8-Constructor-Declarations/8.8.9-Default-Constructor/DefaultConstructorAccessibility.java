/*
 * @test
 * @summary JLS 8.8.9: the implicitly declared default constructor has the same
 *          accessibility as its class.
 * @run main DefaultConstructorAccessibility
 */

import java.lang.reflect.Modifier;

public class DefaultConstructorAccessibility {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Public {}           // default constructor is public
    private static class Private {}         // default constructor is private
    protected static class Protected {}     // default constructor is protected
    static class PackagePrivate {}          // default constructor is package-private

    public static void main(String[] args) throws Exception {
        int pub = Public.class.getDeclaredConstructor().getModifiers();
        int pri = Private.class.getDeclaredConstructor().getModifiers();
        int pro = Protected.class.getDeclaredConstructor().getModifiers();
        int pk  = PackagePrivate.class.getDeclaredConstructor().getModifiers();

        check(Modifier.isPublic(pub), "public class gets public default ctor");
        check(Modifier.isPrivate(pri), "private class gets private default ctor");
        check(Modifier.isProtected(pro), "protected class gets protected default ctor");
        check(!Modifier.isPublic(pk) && !Modifier.isPrivate(pk) && !Modifier.isProtected(pk),
              "package-private class gets package-private default ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
