/*
 * @test
 * @summary JLS 8.4.3.5: the strictfp modifier on a method declaration is
 *          obsolete; its presence has no effect at run time.
 * @compile StrictfpMethod.java
 */

class StrictfpMethod {
    strictfp double half() { return 1.0 / 3.0; }
}
