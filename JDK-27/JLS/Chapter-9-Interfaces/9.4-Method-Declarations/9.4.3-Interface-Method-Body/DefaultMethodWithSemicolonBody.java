/*
 * @test
 * @summary JLS 9.4.3: a default, private, or static interface method has a block
 *          body; a semicolon body for such a method is a compile-time error.
 * @compile/fail DefaultMethodWithSemicolonBody.java
 */

interface DefaultMethodWithSemicolonBody {
    default void m();   // error: default method must have a body
}
