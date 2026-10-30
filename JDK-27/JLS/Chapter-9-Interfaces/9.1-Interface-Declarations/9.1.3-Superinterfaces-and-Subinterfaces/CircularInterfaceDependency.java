/*
 * @test
 * @summary JLS 9.1.3: it is a compile-time error if an interface depends on
 *          itself.
 * @compile/fail CircularInterfaceDependency.java
 */

interface CircularInterfaceDependency extends Other {}
interface Other extends CircularInterfaceDependency {}   // error: cyclic inheritance
