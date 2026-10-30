/*
 * @test
 * @summary JLS 8.7: a static initializer introduces a static context, so the
 *          keyword this is prohibited there.
 * @compile/fail StaticInitializerCannotUseThis.java
 */

class StaticInitializerCannotUseThis {
    static {
        this.toString();   // error: non-static variable this cannot be referenced
    }
}
