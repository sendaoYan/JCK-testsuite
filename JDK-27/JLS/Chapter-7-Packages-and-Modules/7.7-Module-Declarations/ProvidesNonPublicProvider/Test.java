/*
 * @test
 * @summary JLS 7.7.4: every service provider must be a public class or
 *          interface; a package-private provider is a compile-time error.
 * @compile/fail m/module-info.java m/p/S.java m/p/P.java
 */
