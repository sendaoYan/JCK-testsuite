/*
 * @test
 * @summary JLS 6.6 (Java SE 8): access to public, protected, and package-private
 *          members is allowed from the same package; protected members are also
 *          accessible to subclasses.
 * @run main AccessControlTest
 */

class Helper {
    private int priv = 1;         // private: this class only
    int pkg = 2;                  // package-private
    protected int prot = 3;       // protected: package + subclasses
    public int pub = 4;           // public: everyone

    int getPriv() { return priv; }   // private member reachable within its own class
}

public class AccessControlTest extends Helper {
    public static void main(String[] args) {
        Helper h = new Helper();
        if (h.pkg != 2) throw new AssertionError("package-private access");
        if (h.prot != 3) throw new AssertionError("protected access in same package");
        if (h.pub != 4) throw new AssertionError("public access");
        if (h.getPriv() != 1) throw new AssertionError("private member via own accessor");

        AccessControlTest t = new AccessControlTest();
        if (t.prot != 3) throw new AssertionError("protected member via subclass");

        System.out.println("PASS");
    }
}
