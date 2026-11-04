/*
 * @test
 * @summary JLS 15.12.2: "If no method applicable by subtyping is found, the
 *          search for applicable methods continues with strict/loose/built-in
 *          invocations; if no applicable method is found at all, it is a
 *          compile-time error."
 * @compile/fail NoApplicableMethod.java
 */

class NoApplicableMethod {
    static int m(int x) { return x; }
    void test() {
        m("not an int");   // error: no suitable method found for m(String)
    }
}
