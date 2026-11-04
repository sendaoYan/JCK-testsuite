/*
 * @test
 * @summary JLS 15.12.2.8 (with 18.5) (Java SE 8): if the bounds inferred for a
 *          type variable are incompatible, it is a compile-time error.
 * @compile/fail GenericMethodInferenceFail.java
 */

class GenericMethodInferenceFail {
    static <T extends Number> T m() { return null; }
    void test() {
        String s = m();   // error: inference variable T has incompatible bounds (String vs Number)
    }
}
