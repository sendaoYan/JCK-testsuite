/*
 * @test
 * @summary JLS 15.11 (with 6.6) (Java SE 8): a field access expression may not
 *          access a private field of another top-level class.
 * @compile/fail PrivateFieldAccess.java
 */

class Holder {
    private int secret;
}

class PrivateFieldAccess {
    int m(Holder h) {
        return h.secret;   // error: secret has private access in Holder
    }
}
