/*
 * @test
 * @summary JLS 8.6: it is a compile-time error if a return statement appears
 *          anywhere within an instance initializer.
 * @compile/fail ReturnInInstanceInitializer.java
 */

class ReturnInInstanceInitializer {
    {
        return;   // error: return outside a method
    }
}
