/*
 * @test
 * @summary JLS 9.2: it is a compile-time error if an interface explicitly
 *          declares a method that is declared final in Object.
 * @compile/fail InterfaceDeclaresFinalObjectMethod.java
 */

interface InterfaceDeclaresFinalObjectMethod {
    Class<?> getClass();   // error: getClass() in Object is final
}
