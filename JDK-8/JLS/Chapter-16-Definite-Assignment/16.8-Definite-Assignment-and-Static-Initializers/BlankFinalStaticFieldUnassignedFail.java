/*
 * @test
 * @summary JLS 16.8 (Java SE 8): a blank final static field that is never assigned
 *          in a static initializer is a compile-time error.
 * @compile/fail BlankFinalStaticFieldUnassignedFail.java
 */

class BlankFinalStaticFieldUnassignedFail {
    static final int V;   // error: variable V might not have been initialized
}
