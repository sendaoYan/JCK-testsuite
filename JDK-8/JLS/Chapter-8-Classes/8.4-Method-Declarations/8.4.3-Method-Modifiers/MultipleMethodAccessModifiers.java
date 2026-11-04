/*
 * @test
 * @summary JLS 8.4.3 (Java SE 8): it is a compile-time error if a method declaration has
 *          more than one of the access modifiers public, protected, and private.
 * @compile/fail MultipleMethodAccessModifiers.java
 */
class MultipleMethodAccessModifiers {
    public private void m() { }   // error: illegal combination of modifiers
}
