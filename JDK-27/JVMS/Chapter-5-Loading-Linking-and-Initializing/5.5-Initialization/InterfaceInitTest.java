/*
 * @test
 * @summary JVMS 5.5 / JLS 12.4.1: reading a constant variable does not
 *          initialize an interface, but reading a non-constant field does;
 *          initializing an interface does not initialize its superinterfaces;
 *          initializing a class initializes a superinterface that declares a
 *          default method but not one that declares no default methods.
 * @run main InterfaceInitTest
 */

public class InterfaceInitTest {
    static boolean iInit = false, superInit = false, subInit = false;
    static boolean defSuperInit = false, noDefSuperInit = false, classInit = false;

    static boolean initI()        { iInit = true; return true; }
    static boolean initSuper()    { superInit = true; return true; }
    static boolean initSub()      { subInit = true; return true; }
    static boolean initDefSuper() { defSuperInit = true; return true; }
    static boolean initNoDefSuper(){ noDefSuperInit = true; return true; }
    static boolean initClass()    { classInit = true; return true; }

    interface I        { int CONST = 42; boolean FLAG = initI(); }
    interface Super    { boolean S = initSuper(); }
    interface Sub extends Super { boolean T = initSub(); }
    interface DefSuper  { default void m() {} boolean D = initDefSuper(); }
    interface NoDefSuper{ boolean N = initNoDefSuper(); }
    static class Impl implements DefSuper, NoDefSuper {
        static boolean C = initClass();
    }

    public static void main(String[] args) {
        // 1. Constant variable access inlines the value; interface not initialized.
        if (I.CONST != 42) throw new AssertionError("CONST value");
        if (iInit) throw new AssertionError("constant access initialized I");

        // 2. Non-constant field access does initialize the interface.
        if (!I.FLAG) throw new AssertionError("FLAG value");
        if (!iInit) throw new AssertionError("non-constant access did not initialize I");

        // 3. Initializing Sub does not initialize its superinterface Super.
        if (!Sub.T) throw new AssertionError("Sub.T value");
        if (!subInit) throw new AssertionError("Sub not initialized");
        if (superInit) throw new AssertionError("initializing Sub initialized Super");
        if (!Super.S) throw new AssertionError("Super.S value");
        if (!superInit) throw new AssertionError("Super not initialized on direct use");

        // 4. Initializing Impl initializes its default-method superinterface but
        //    not its plain superinterface.
        if (!Impl.C) throw new AssertionError("Impl.C value");
        if (!classInit) throw new AssertionError("Impl not initialized");
        if (!defSuperInit) throw new AssertionError("default-method superinterface not initialized");
        if (noDefSuperInit) throw new AssertionError("plain superinterface was initialized");

        System.out.println("PASS");
    }
}
