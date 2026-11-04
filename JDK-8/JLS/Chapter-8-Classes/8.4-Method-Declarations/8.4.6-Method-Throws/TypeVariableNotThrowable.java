/*
 * @test
 * @summary JLS 8.4.6 (Java SE 8): it is a compile-time error if an ExceptionType in a throws
 *          clause is not a subtype of Throwable (an unconstrained type variable).
 * @compile/fail TypeVariableNotThrowable.java
 */
class TypeVariableNotThrowable {
    <E> void m() throws E { }   // error: incompatible types: E cannot be converted to Throwable
}
