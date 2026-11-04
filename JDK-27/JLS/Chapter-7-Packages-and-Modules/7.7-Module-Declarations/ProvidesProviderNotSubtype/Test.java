/*
 * @test
 * @summary JLS 7.7.4: a service provider without a provider method must have a
 *          provider constructor and be a subtype of the service; a non-subtype
 *          provider is a compile-time error.
 * @compile/fail m/module-info.java m/p/S.java m/p/P.java
 */
