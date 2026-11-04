/*
 * @test
 * @summary JLS 15.8.3: "It is a compile-time error if a this expression occurs
 *          in a static context."  The keyword this may not be referenced from a
 *          static method.
 * @compile/fail ThisInStaticContext.java
 */

class ThisInStaticContext {
    int x = 1;
    static int m() {
        return this.x;   // error: non-static variable this cannot be referenced from a static context
    }
}
