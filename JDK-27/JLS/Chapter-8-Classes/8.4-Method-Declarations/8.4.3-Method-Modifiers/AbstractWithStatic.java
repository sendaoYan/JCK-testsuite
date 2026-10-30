/*
 * @test
 * @summary JLS 8.4.3: a method declaration that contains the keyword abstract
 *          may not also contain private, static, final, native, strictfp, or
 *          synchronized.
 * @compile/fail AbstractWithStatic.java
 */

class AbstractWithStatic {
    abstract static void m();   // error: illegal combination of modifiers
}
