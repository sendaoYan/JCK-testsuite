/*
 * @test
 * @summary JLS 15.12.2.8 (with 18.5): if the bounds inferred for a type variable
 *          are incompatible (here the target type requires String while the
 *          declared bound requires Number), it is a compile-time error.
 * @compile/fail GenericMethodInferenceFail.java
 */

class GenericMethodInferenceFail {
    static <T extends Number> T m() { return null; }
    void test() {
        String s = m();   // error: inference variable T has incompatible bounds (String vs Number)
    }
}
