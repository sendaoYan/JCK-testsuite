/*
 * @test
 * @summary JLS 8.1.2: it is a compile-time error if a type variable in a class's
 *          type parameter section depends on itself.
 * @compile/fail TypeVariableDependsOnItself.java
 */
class TypeVariableDependsOnItself<T extends T> { }   // error: cyclic inheritance involving T
