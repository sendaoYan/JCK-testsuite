/*
 * @test
 * @summary JLS 9.6.4.7: it is a compile-time error to annotate a fixed arity
 *          method with SafeVarargs.
 * @compile/fail SafeVarargsFixedArity.java
 */

class SafeVarargsFixedArity {
    @SafeVarargs
    static <T> void m(T t) {}   // error: @SafeVarargs on a fixed arity method
}
