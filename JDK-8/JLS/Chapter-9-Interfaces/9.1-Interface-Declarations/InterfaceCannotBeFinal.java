/*
 * @test
 * @summary JLS 9.1.1 (Java SE 8): unlike a class, an interface cannot be declared
 *          final.
 * @compile/fail InterfaceCannotBeFinal.java
 */

final interface InterfaceCannotBeFinal {}   // error: interface cannot be final
