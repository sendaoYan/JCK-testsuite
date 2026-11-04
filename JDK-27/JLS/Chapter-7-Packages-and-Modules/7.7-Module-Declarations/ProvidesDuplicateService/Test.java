/*
 * @test
 * @summary JLS 7.7.4: it is a compile-time error if more than one provides
 *          directive in a module declaration specifies the same service.
 * @compile/fail m/module-info.java m/p/S.java m/p/P.java
 */
