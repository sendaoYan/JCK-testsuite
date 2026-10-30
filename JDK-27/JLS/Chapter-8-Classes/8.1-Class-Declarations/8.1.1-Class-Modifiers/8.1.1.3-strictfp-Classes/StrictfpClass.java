/*
 * @test
 * @summary JLS 8.1.1.3: the strictfp modifier on a class declaration is obsolete;
 *          its presence has no effect at compile time or run time.
 * @compile StrictfpClass.java
 */

strictfp class StrictfpClass {
    double half() { return 1.0 / 3.0; }
}
