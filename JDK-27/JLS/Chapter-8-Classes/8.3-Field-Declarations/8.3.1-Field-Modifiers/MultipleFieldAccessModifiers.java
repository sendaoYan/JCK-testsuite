/*
 * @test
 * @summary JLS 8.3.1: it is a compile-time error if a field declaration has more
 *          than one of the access modifiers public, protected, and private.
 * @compile/fail MultipleFieldAccessModifiers.java
 */
class MultipleFieldAccessModifiers {
    public private int f;   // error: illegal combination of modifiers
}
