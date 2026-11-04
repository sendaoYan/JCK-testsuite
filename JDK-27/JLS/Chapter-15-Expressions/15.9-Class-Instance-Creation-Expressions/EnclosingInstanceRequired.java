/*
 * @test
 * @summary JLS 15.9.2: an unqualified class instance creation expression for a
 *          non-static inner class must be lexically within an instance context;
 *          from a static context it is a compile-time error because there is no
 *          enclosing instance.
 * @compile/fail EnclosingInstanceRequired.java
 */

class EnclosingInstanceRequired {
    class Inner { }
    static Inner m() {
        return new Inner();   // error: non-static variable this cannot be referenced from a static context
    }
}
