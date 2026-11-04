/*
 * @test
 * @summary JLS 7.7.1: if a module expresses a dependence on java.base and is not
 *          itself java.base, it is a compile-time error if the static modifier
 *          appears after the requires keyword.
 * @compile/fail m/module-info.java
 */
