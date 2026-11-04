/*
 * @test
 * @summary JLS 7.7.4: it is a compile-time error if the with clause of a given
 *          provides directive specifies the same service provider more than
 *          once.
 * @compile/fail m/module-info.java m/p/S.java m/p/P.java
 */
