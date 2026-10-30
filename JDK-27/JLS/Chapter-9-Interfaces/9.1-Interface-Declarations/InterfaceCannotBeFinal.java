/*
 * @test
 * @summary JLS 9.1: unlike a class, an interface cannot be declared final.
 * @compile/fail InterfaceCannotBeFinal.java
 */

final interface InterfaceCannotBeFinal {}   // error: interface cannot be final
