/*
 * @test
 * @summary JLS 8.3.1.1 / 8.3.2: a class variable initializer may not refer to the
 *          current object using this.
 * @compile/fail StaticFieldInitializerCannotUseThis.java
 */
class StaticFieldInitializerCannotUseThis {
    int x;
    static int y = this.x;   // error: non-static variable this cannot be referenced from a static context
}
