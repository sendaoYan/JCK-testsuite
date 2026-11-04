/*
 * @test
 * @summary JLS 7.7.2: it is permitted (a compile-time-valid construct) for an
 *          opens directive to specify a package which is not declared by a
 *          compilation unit associated with the current module; only exports
 *          requires the package to be declared.
 * @compile m/module-info.java
 */
