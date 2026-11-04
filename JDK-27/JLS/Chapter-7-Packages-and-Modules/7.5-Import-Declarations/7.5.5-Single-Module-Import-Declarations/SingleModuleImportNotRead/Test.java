/*
 * @test
 * @summary JLS 7.5.5: it is a compile-time error if the module named in a
 *          single-module-import declaration is not read by the current module
 *          (module m does not read: java.sql).
 * @compile/fail m/module-info.java m/p/C.java
 */
