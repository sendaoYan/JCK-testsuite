/*
 * @test
 * @summary java.lang.foreign.FunctionDescriptor behavior against a real native
 *          linker: descriptor/handle type correspondence, derived descriptors
 *          (append/insert/change/drop) driving genuinely invoked downcalls,
 *          and layout-count mismatches.
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED FunctionDescriptorNativeTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class FunctionDescriptorNativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    static final String C_SRC = """
            int jck_sum2(int a, int b) { return a + 2 * b; }
            long jck_ptr_and_int(const void *p, int v) { return (long) v + (p != 0); }
            int jck_sum3(int a, int b, int c) { return 100 * a + 10 * b + c; }
            long jck_widen(int a) { return (long) a; }
            void jck_void3(int a, int b, int c) { (void) (a + b + c); }
            """;

    static Path compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("FunctionDescriptorNativeTest_jckffm.c");
        Path so = dir.resolve("libFunctionDescriptorNativeTest_jckffm.so");
        Files.writeString(src, C_SRC);
        Process p = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), src.toString()).inheritIO().start();
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new IllegalStateException("gcc exited " + p.exitValue());
        return so;
    }

    static Linker LINKER;
    static SymbolLookup OWN;

    static MemorySegment sym(String name) { return OWN.findOrThrow(name); }

    // ---- toMethodType() equals the bound handle's type; unbound form prepends the address ----
    static void testTypeCorrespondence() throws Throwable {
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
        MethodType fdType = fd.toMethodType();
        check(fdType.equals(MethodType.methodType(long.class, MemorySegment.class, int.class)),
                "descriptor (ADDRESS, int) -> long maps to (MemorySegment, int) -> long");
        MethodHandle bound = LINKER.downcallHandle(sym("jck_ptr_and_int"), fd);
        check(bound.type().equals(fdType), "bound handle type equals FunctionDescriptor.toMethodType()");
        MethodHandle unbound = LINKER.downcallHandle(fd);
        check(unbound.type().equals(fdType.insertParameterTypes(0, MemorySegment.class)),
                "unbound handle type is the descriptor type with a leading target address");
        try (Arena a = Arena.ofConfined()) {
            MemorySegment p = a.allocate(4, 4);
            check((long) bound.invoke(p, 7) == 8L, "bound handle invoked: 7 + non-null pointer == 8");
            check((long) bound.invoke(MemorySegment.NULL, 7) == 7L, "bound handle invoked: 7 + NULL pointer == 7");
            check((long) unbound.invoke(sym("jck_ptr_and_int"), p, 7) == 8L,
                    "unbound handle invoked with explicit target address");
        }
    }

    // ---- appendArgumentLayouts grows a descriptor and the linked call follows ----
    static void testAppend() throws Throwable {
        FunctionDescriptor two = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        FunctionDescriptor three = two.appendArgumentLayouts(ValueLayout.JAVA_INT);
        check(three.argumentLayouts().size() == 3, "appendArgumentLayouts adds one layout");
        check(three.argumentLayouts().get(2) == ValueLayout.JAVA_INT, "appended layout is last");
        MethodHandle sum2Only = LINKER.downcallHandle(sym("jck_sum2"), two);
        MethodHandle sum3 = LINKER.downcallHandle(sym("jck_sum3"), three);
        check((int) sum2Only.invoke(1, 2) == 5, "original descriptor still links jck_sum2: 1+2*2 == 5");
        check((int) sum3.invoke(1, 2, 3) == 123, "appended descriptor links jck_sum3: 100+20+3 == 123");
    }

    // ---- insertArgumentLayouts repositions parameters for the linked call ----
    static void testInsert() throws Throwable {
        FunctionDescriptor two = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        FunctionDescriptor insertedMiddle = two.insertArgumentLayouts(1, ValueLayout.JAVA_INT);
        check(insertedMiddle.argumentLayouts().size() == 3, "insertArgumentLayouts adds one layout");
        check(insertedMiddle.argumentLayouts().get(1) == ValueLayout.JAVA_INT, "inserted layout at index 1");
        MethodHandle middle = LINKER.downcallHandle(sym("jck_sum3"), insertedMiddle);
        // layouts map positionally after the insertion: (4, 5, 6) -> a=4, b=5, c=6
        check((int) middle.invoke(4, 5, 6) == 456, "inserted-middle descriptor: 100*4+10*5+6 == 456");
        FunctionDescriptor insertedHead = two.insertArgumentLayouts(0, ValueLayout.JAVA_INT);
        MethodHandle head = LINKER.downcallHandle(sym("jck_sum3"), insertedHead);
        check((int) head.invoke(7, 8, 9) == 789, "inserted-head descriptor: 100*7+10*8+9 == 789");
        for (int bad : new int[]{-1, 3}) {
            try {
                two.insertArgumentLayouts(bad, ValueLayout.JAVA_INT);
                check(false, "insertArgumentLayouts(" + bad + ") must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "insertArgumentLayouts(" + bad + ") throws IllegalArgumentException");
            }
        }
    }

    // ---- changeReturnLayout changes what the linked call hands back ----
    static void testChangeReturn() throws Throwable {
        FunctionDescriptor intReturn = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        FunctionDescriptor longReturn = intReturn.changeReturnLayout(ValueLayout.JAVA_LONG);
        check(longReturn.returnLayout().orElseThrow() == ValueLayout.JAVA_LONG, "changeReturnLayout swaps return");
        check(intReturn.returnLayout().orElseThrow() == ValueLayout.JAVA_INT, "original descriptor unchanged");
        MethodHandle widened = LINKER.downcallHandle(sym("jck_widen"), longReturn);
        check(widened.type().returnType() == long.class, "handle return type follows new layout");
        check((long) widened.invoke(Integer.MAX_VALUE) == (long) Integer.MAX_VALUE,
                "C int return widened to long by descriptor: Integer.MAX_VALUE survives");
    }

    // ---- dropReturnLayout yields a void invocation that discards C's value ----
    static void testDropReturn() throws Throwable {
        FunctionDescriptor withRet = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        FunctionDescriptor voidCall = withRet.dropReturnLayout();
        check(voidCall.argumentLayouts().size() == 3, "dropReturnLayout keeps all argument layouts");
        check(voidCall.returnLayout().isEmpty(), "dropReturnLayout empties the return layout");
        MethodHandle voidHandle = LINKER.downcallHandle(sym("jck_sum3"), voidCall);
        check(voidHandle.type().returnType() == void.class, "handle type returns void after drop");
        voidHandle.invoke(1, 2, 3);
        check(true, "void downcall of an int-returning C function completes");
        // ofVoid is the direct spelling of the same contract
        MethodHandle ofVoidHandle = LINKER.downcallHandle(sym("jck_void3"),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        check(ofVoidHandle.type().returnType() == void.class, "ofVoid descriptor links a void handle");
        ofVoidHandle.invoke(4, 5, 6);
        check(true, "ofVoid downcall completes");
    }

    // ---- argument count mismatches surface at invocation, not linkage ----
    static void testInvocationArityMismatch() throws Throwable {
        MethodHandle two = LINKER.downcallHandle(sym("jck_sum2"),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        try {
            two.invoke(1);
            check(false, "invoking a downcall with too few arguments must throw");
        } catch (WrongMethodTypeException expected) {
            check(true, "under-invocation throws WrongMethodTypeException");
        }
    }

    public static void main(String[] args) throws Throwable {
        if (!gccAvailable()) {
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        LINKER = Linker.nativeLinker();
        System.load(compileLib().toString());
        OWN = SymbolLookup.loaderLookup();
        testTypeCorrespondence();
        testAppend();
        testInsert();
        testChangeReturn();
        testDropReturn();
        testInvocationArityMismatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
