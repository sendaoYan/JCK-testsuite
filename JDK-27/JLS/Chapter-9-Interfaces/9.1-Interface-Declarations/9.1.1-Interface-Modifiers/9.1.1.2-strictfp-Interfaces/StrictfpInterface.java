/*
 * @test
 * @summary JLS 9.1.1.2: the strictfp modifier on an interface declaration is
 *          obsolete and has no effect at compile time or run time.
 * @compile StrictfpInterface.java
 */

strictfp interface StrictfpInterface {
    double half();
}
