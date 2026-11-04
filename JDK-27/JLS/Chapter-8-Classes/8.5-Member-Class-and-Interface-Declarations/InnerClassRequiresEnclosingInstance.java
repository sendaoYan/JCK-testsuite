/*
 * @test
 * @summary JLS 8.5: an inner class (a non-static member class) is associated with
 *          an instance of its enclosing class; creating one without an enclosing
 *          instance is a compile-time error.
 * @compile/fail InnerClassRequiresEnclosingInstance.java
 */

class Outer {
    class Inner {}
}

class InnerClassRequiresEnclosingInstance {
    Outer.Inner bad = new Outer.Inner();   // error: an enclosing instance that contains Outer.Inner is required
}
