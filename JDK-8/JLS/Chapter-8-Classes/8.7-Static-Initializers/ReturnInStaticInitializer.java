/*
 * @test
 * @summary JLS 8.7 (Java SE 8): it is a compile-time error if a return statement
 *          appears anywhere within a static initializer.
 * @compile/fail ReturnInStaticInitializer.java
 */

class ReturnInStaticInitializer {
    static {
        return;   // error: return outside a method
    }
}
