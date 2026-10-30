/*
 * @test
 * @summary JLS 6.6 (Java SE 8): a private member is not accessible from another
 *          top-level class, even one in the same package.
 * @compile/fail PrivateAccessFail.java
 */

class Holder {
    private int priv = 1;
}

class PrivateAccessFail {
    void m() {
        Holder h = new Holder();
        int x = h.priv;   // error: priv has private access in Holder
    }
}
