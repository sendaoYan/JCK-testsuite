/*
 * @test
 * @summary JLS 15.9.2 (Java SE 8): an unqualified creation of a non-static
 *          inner class from a static context is a compile-time error.
 * @compile/fail EnclosingInstanceRequired.java
 */

class EnclosingInstanceRequired {
    class Inner { }
    static Inner m() {
        return new Inner();   // error: non-static variable this cannot be referenced from a static context
    }
}
