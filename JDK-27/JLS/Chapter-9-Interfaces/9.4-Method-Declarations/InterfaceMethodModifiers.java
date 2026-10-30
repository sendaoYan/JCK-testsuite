/*
 * @test
 * @summary JLS 9.4: interface methods may be abstract (implicitly public),
 *          default, static, or private; private methods are callable only from
 *          within the interface.
 * @run main InterfaceMethodModifiers
 */

public class InterfaceMethodModifiers {
    interface I {
        int a();                        // abstract, implicitly public
        default int d() { return 2; }   // default
        static int s() { return 3; }    // static
        private int p() { return 4; }   // private instance
        default int viaPrivate() { return p(); }
        private static int ps() { return 5; }
        static int viaPrivateStatic() { return ps(); }
    }

    static class Impl implements I {
        public int a() { return 1; }
    }

    public static void main(String[] args) {
        Impl i = new Impl();
        if (i.a() != 1) throw new AssertionError("abstract");
        if (i.d() != 2) throw new AssertionError("default");
        if (I.s() != 3) throw new AssertionError("static");
        if (i.viaPrivate() != 4) throw new AssertionError("private via default");
        if (I.viaPrivateStatic() != 5) throw new AssertionError("private static");
        System.out.println("PASS");
    }
}
