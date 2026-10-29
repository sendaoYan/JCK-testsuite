/*
 * @test
 * @summary JLS 7.7.6: a module is observable if the compilation unit containing
 *          its declaration is observable. java.sql is observable, so it can be
 *          required and is present in the boot layer's resolved configuration.
 * @build m/*
 * @run main m/p.Main
 */
