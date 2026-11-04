/*
 * @test
 * @summary JLS 9.1.3 (Java SE 8): an interface may extend only other interfaces, not
 *          classes.
 * @compile/fail InterfaceCannotExtendClass.java
 */

class Base {}

interface InterfaceCannotExtendClass extends Base {}   // error: interface expected here
