/*
 * @test
 * @summary JLS 4.4: a type variable is an unqualified identifier introduced by
 *          a type parameter. With no declared bound, Object is assumed. With a
 *          bound "T & I1 & ... & In", the members of the type variable are the
 *          members of the intersection type T & I1 & ... & In.
 * @run main TypeVariables
 */

class C {
    public void mCPublic() {}
    protected void mCProtected() {}
    void mCPackage() {}
    private void mCPrivate() {}
}

interface I {
    void mI();
}

class CT extends C implements I {
    public void mI() {}
}

public class TypeVariables {
    // T's bound induces the intersection type C & I; members include mI, mCPublic,
    // mCProtected, mCPackage (but not the private mCPrivate, Example 4.4-1).
    <T extends C & I> int test(T t) {
        t.mI();
        t.mCPublic();
        t.mCProtected();
        t.mCPackage();
        return 1;
    }

    // No bound declared: Object is assumed.
    <T> T identity(T x) { return x; }

    public static void main(String[] args) {
        TypeVariables tv = new TypeVariables();
        CT ct = new CT();
        if (tv.test(ct) != 1) throw new AssertionError("members of intersection bound");

        // Default bound is Object.
        if (!tv.identity("x").equals("x")) throw new AssertionError("identity");
        if (!(tv.identity(new Object()).getClass() == Object.class)) {
            throw new AssertionError("default bound must be Object");
        }

        System.out.println("PASS");
    }
}
