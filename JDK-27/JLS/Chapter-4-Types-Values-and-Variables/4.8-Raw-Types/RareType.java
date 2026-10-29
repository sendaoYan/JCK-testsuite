/*
 * @test
 * @summary JLS 4.8: "It is not possible to access Inner as a partially raw type
 *          (a 'rare' type)": because Outer itself is raw, so are all its inner
 *          classes, so passing type arguments to Inner is a compile-time error.
 * @compile/fail RareType.java
 */

class Outer<T> {
    class Inner<S> {
        S s;
    }
}

class RareType {
    void m() {
        Outer.Inner<Double> x = null;   // error: illegal "rare" type
    }
}
