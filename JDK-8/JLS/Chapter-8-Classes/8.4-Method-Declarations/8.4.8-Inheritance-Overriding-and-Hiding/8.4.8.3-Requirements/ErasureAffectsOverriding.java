/*
 * @test
 * @summary JLS 8.4.8.3 (Example 8.4.8.3-4): a class cannot have two member
 *          methods with the same name and type erasure; D.id(Object) clashes
 *          with the inherited C<String>.id(String).
 * @compile/fail ErasureAffectsOverriding.java
 */

class C<T> {
    T id(T x) { return x; }
}

class ErasureAffectsOverriding extends C<String> {
    // D.id(Object) and inherited C<String>.id(String) have the same erasure,
    // yet neither is a subsignature of the other -> name clash
    Object id(Object x) { return x; }
}
