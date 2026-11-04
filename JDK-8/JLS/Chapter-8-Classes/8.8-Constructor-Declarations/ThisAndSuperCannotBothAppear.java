/*
 * @test
 * @summary JLS 8.8.7.1 (Java SE 8): a constructor body may not contain both an
 *          alternate constructor invocation (this) and a superclass constructor
 *          invocation (super).
 * @compile/fail ThisAndSuperCannotBothAppear.java
 */

class Base {
    Base(int x) {}
}

class ThisAndSuperCannotBothAppear extends Base {
    ThisAndSuperCannotBothAppear() {
        this(1);
        super(1);   // error: redundant explicit constructor invocation
    }
    ThisAndSuperCannotBothAppear(int x) {
        super(x);
    }
}
