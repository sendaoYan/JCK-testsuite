/*
 * @test
 * @summary JLS 7.7.1: the requires transitive modifier causes any module that
 *          requires the current module to have an implicitly declared dependence
 *          on the specified module; mA reads mC (and its exported package p)
 *          only through mB's requires transitive mC.
 * @build mA/* mB/* mC/*
 * @run main mA/client.Test
 */
