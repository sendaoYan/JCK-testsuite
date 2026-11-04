/*
 * @test
 * @summary JLS 15.12.2 (Java SE 8): if no applicable method is found at all, it
 *          is a compile-time error.
 * @compile/fail NoApplicableMethod.java
 */

class NoApplicableMethod {
    static int m(int x) { return x; }
    void test() {
        m("not an int");   // error: no suitable method found for m(String)
    }
}
