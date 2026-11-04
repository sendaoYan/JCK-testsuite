/*
 * @test
 * @summary JLS 8.1.1 (Java SE 8): it is a compile-time error if a class declaration has
 *          more than one of the access modifiers public, protected, and private.
 * @compile/fail MultipleAccessModifiers.java
 */
class MultipleAccessModifiers {
    public private class Inner { }   // error: illegal combination of modifiers
}
