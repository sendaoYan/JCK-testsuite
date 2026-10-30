/*
 * @test
 * @summary JLS 8.1.2: it is a compile-time error if a type variable in a class's
 *          type parameter section depends on itself.
 * @compile/fail CyclicTypeVariableBound.java
 */

class CyclicTypeVariableBound<T extends T> {}   // error: cyclic type variable bound
