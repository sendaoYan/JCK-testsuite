/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup conformance: white-box + black-box behavior.
 * @run main LookupTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LookupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Holder {
        public int x = 7;
        public static int STAT = 100;
        public String name;
        public Holder() { name = "ctor"; }
        public int size() { return 42; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        for (int k : new int[]{1, 2, 4, 8, 16, 32, 64}) {
            check(c.getField(modeName(k)).getType() == int.class, "mode constant " + k);
        }
        String[] names = {"lookupClass", "lookupModes", "in", "dropLookupMode",
                "findStatic", "findVirtual", "findConstructor", "findSpecial", "findGetter",
                "findSetter", "findVarHandle", "findStaticGetter", "findStaticSetter",
                "findClass", "accessClass", "ensureInitialized", "defineClass",
                "defineHiddenClass", "bind", "unreflect", "unreflectGetter", "unreflectSetter",
                "unreflectConstructor", "unreflectVarHandle", "revealDirect", "toString",
                "hasPrivateAccess", "hasFullPrivilegeAccess"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    static String modeName(int v) {
        switch (v) {
            case 1: return "PUBLIC";
            case 2: return "PRIVATE";
            case 4: return "PROTECTED";
            case 8: return "PACKAGE";
            case 16: return "MODULE";
            case 32: return "UNCONDITIONAL";
            default: return "ORIGINAL";
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        Lookup lookup = MethodHandles.lookup();

        // identity of the lookup and its access modes
        check(lookup.lookupClass() == LookupTest.class, "lookupClass");
        check(lookup.lookupModes() == (Lookup.PUBLIC | Lookup.PRIVATE | Lookup.PROTECTED
                | Lookup.PACKAGE | Lookup.MODULE | Lookup.ORIGINAL),
              "lookup() has full modes: " + lookup.lookupModes());
        check(lookup.hasPrivateAccess(), "hasPrivateAccess");
        check(lookup.hasFullPrivilegeAccess(), "hasFullPrivilegeAccess");
        check(MethodHandles.publicLookup().lookupModes() == Lookup.UNCONDITIONAL,
              "publicLookup has only UNCONDITIONAL");

        // toString mentions the lookup class
        check(lookup.toString().contains("LookupTest"), "toString names class");

        // findVirtual / findStatic / findConstructor / access
        MethodHandle size = lookup.findVirtual(Holder.class, "size",
                MethodType.methodType(int.class));
        check((int) size.invoke(new Holder()) == 42, "findVirtual");
        MethodHandle parseInt = lookup.findStatic(Integer.class, "parseInt",
                MethodType.methodType(int.class, String.class));
        check((int) parseInt.invokeExact("77") == 77, "findStatic");
        MethodHandle ctor = lookup.findConstructor(Holder.class,
                MethodType.methodType(void.class));
        check(((Holder) ctor.invokeExact()).name.equals("ctor"), "findConstructor");
        check(lookup.findClass("java.lang.String") == String.class, "findClass");
        check(lookup.accessClass(Holder.class) == Holder.class, "accessClass");

        // field access
        MethodHandle getX = lookup.findGetter(Holder.class, "x", int.class);
        Holder h = new Holder();
        check((int) getX.invoke(h) == 7, "findGetter");
        MethodHandle setX = lookup.findSetter(Holder.class, "x", int.class);
        setX.invoke(h, 11);
        check(h.x == 11, "findSetter");
        MethodHandle stat = lookup.findStaticGetter(Holder.class, "STAT", int.class);
        check((int) stat.invoke() == 100, "findStaticGetter");
        check((int) lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class)).invokeExact((String) "abcd") == 4,
              "findVirtual on String");

        // findVarHandle gives a working VarHandle
        java.lang.invoke.VarHandle vh = lookup.findVarHandle(Holder.class, "x", int.class);
        check((int) vh.get(h) == 11, "findVarHandle get");
        vh.set(h, 21);
        check(h.x == 21, "findVarHandle set");

        // unreflect a reflective Method
        Method nameLen = String.class.getMethod("length");
        MethodHandle un = lookup.unreflect(nameLen);
        check((int) un.invokeExact((String) "xyz") == 3, "unreflect");

        // bind attaches a virtual method to a receiver
        MethodHandle bound = lookup.bind("abcde", "length",
                MethodType.methodType(int.class));
        check((int) bound.invokeExact() == 5, "bind");

        // revealDirect round trips to MethodHandleInfo
        MethodHandleInfo info = lookup.revealDirect(parseInt);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "revealDirect kind");
        check(info.getDeclaringClass() == Integer.class, "revealDirect declaring class");
        check(info.getName().equals("parseInt"), "revealDirect name");

        // in() restricts modes to the intersection
        Lookup inString = lookup.in(String.class);
        check(inString.lookupClass() == String.class, "in changes lookupClass");
        check(inString.lookupModes() == Lookup.PUBLIC, "in(String) keeps PUBLIC only");
        // and loses private access to our own members
        try {
            inString.findStatic(LookupTest.class, "secret",
                    MethodType.methodType(int.class));
            check(false, "restricted lookup must not see our private static");
        } catch (IllegalAccessException e) {
            check(true, "restricted lookup denied private member");
        }
        check(lookup.in(LookupTest.class).lookupModes() == lookup.lookupModes(),
              "in(same class) keeps modes");

        // dropLookupMode removes modes
        check(lookup.dropLookupMode(Lookup.PRIVATE).lookupModes()
                      == ((lookup.lookupModes() & ~(Lookup.PRIVATE | Lookup.PROTECTED
                              | Lookup.ORIGINAL))),
              "dropLookupMode(PRIVATE) drops PROTECTED and ORIGINAL too");
        check(lookup.dropLookupMode(Lookup.PROTECTED).lookupModes()
                      == ((lookup.lookupModes() & ~(Lookup.PROTECTED | Lookup.ORIGINAL))),
              "dropLookupMode(PROTECTED)");

        // privateLookupIn grants access into a public class in our own package/module
        Lookup intoHolder = MethodHandles.privateLookupIn(Holder.class, lookup);
        check(intoHolder.lookupClass() == Holder.class, "privateLookupIn target class");
        check(intoHolder.hasPrivateAccess(), "privateLookupIn grants private access");

        // failed lookups report precise exceptions
        try {
            lookup.findVirtual(String.class, "noSuchMethod", MethodType.methodType(int.class));
            check(false, "findVirtual missing member must throw");
        } catch (NoSuchMethodException e) {
            check(true, "NoSuchMethodException");
        }
        try {
            lookup.findGetter(String.class, "value", int.class);
            check(false, "findGetter wrong type must throw");
        } catch (NoSuchFieldException e) {
            check(true, "NoSuchFieldException");
        }
    }

    private static int secret() { return 1; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
