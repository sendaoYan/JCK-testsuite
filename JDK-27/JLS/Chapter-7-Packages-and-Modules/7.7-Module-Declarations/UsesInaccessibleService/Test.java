/*
 * @test
 * @summary JLS 7.7.3: if the service of a uses directive is not declared in the
 *          current module, it must be accessible to the current module; a
 *          service from a module the current module does not read is a
 *          compile-time error.
 * @compile/fail m/module-info.java
 */
