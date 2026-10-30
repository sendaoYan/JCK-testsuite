/*
 * @test
 * @summary JLS 9.1.1.4: a class that is not a permitted direct subclass of a
 *          sealed interface may not implement it.
 * @compile/fail SealedInterfaceDirectSubclassNotPermitted.java
 */

sealed interface Shape permits Circle {}

final class Circle implements Shape {}
final class Triangle implements Shape {}   // error: not permitted
