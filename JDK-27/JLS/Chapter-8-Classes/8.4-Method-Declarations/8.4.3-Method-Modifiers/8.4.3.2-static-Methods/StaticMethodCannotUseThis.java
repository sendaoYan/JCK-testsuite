/*
 * @test
 * @summary JLS 8.4.3.2: the declaration of a static method introduces a static
 *          context; the keyword this is prohibited there.
 * @compile/fail StaticMethodCannotUseThis.java
 */

class StaticMethodCannotUseThis {
    static void m() {
        this.toString();   // error: non-static variable this cannot be referenced
    }
}
