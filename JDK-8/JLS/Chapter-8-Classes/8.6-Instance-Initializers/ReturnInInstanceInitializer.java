/*
 * @test
 * @summary JLS 8.6 (Java SE 8): it is a compile-time error if a return statement
 *          appears anywhere within an instance initializer.
 * @compile/fail ReturnInInstanceInitializer.java
 */

class ReturnInInstanceInitializer {
    {
        return;   // error: return outside a method
    }
}
