/*
 * @test
 * @summary JLS 8.1.6: it is a compile-time error if a class declaration has a
 *          permits clause but no sealed modifier.
 * @compile/fail PermitsWithoutSealed.java
 */

class PermitsWithoutSealed permits B {}   // error: permits without sealed

class B {}
