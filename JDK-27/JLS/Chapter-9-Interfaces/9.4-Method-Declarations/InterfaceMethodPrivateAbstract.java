/*
 * @test
 * @summary JLS 9.4: it is a compile-time error if an interface method declaration
 *          contains both private and abstract.
 * @compile/fail InterfaceMethodPrivateAbstract.java
 */

interface InterfaceMethodPrivateAbstract {
    private abstract void m();   // error: illegal combination of modifiers
}
