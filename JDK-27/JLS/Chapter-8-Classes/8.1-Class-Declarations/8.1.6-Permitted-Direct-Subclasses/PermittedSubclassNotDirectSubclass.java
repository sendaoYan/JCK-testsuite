/*
 * @test
 * @summary JLS 8.1.6: every permitted direct subclass specified by a permits
 *          clause must be a direct subclass of the sealed class.
 * @compile/fail PermittedSubclassNotDirectSubclass.java
 */

sealed class A permits B {}

class B {}   // error: B is not a direct subclass of A
