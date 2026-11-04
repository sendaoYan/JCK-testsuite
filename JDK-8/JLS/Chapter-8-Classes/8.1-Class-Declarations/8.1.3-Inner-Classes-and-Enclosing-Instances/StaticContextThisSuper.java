/*
 * @test
 * @summary JLS 8.1.3 (Java SE 8): a static context prohibits unqualified this and super;
 *          instance members of the enclosing class are not available there.
 * @compile/fail StaticContextThisSuper.java
 */
class StaticContextThisSuper {
    int x = 1;
    static int f() {
        return this.x;   // error: non-static variable this cannot be referenced from a static context
    }
}
