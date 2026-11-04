/*
 * @test
 * @summary java.lang.invoke.MethodType conformance: full contract coverage.
 *          all 6 methodType factory overloads (rtype only / rtype+ptype0 / varargs /
 *          array / List / rtype+MethodType), genericMethodType variants, factory
 *          validation (null rtype/ptype/element -> NPE, void parameter -> IAE,
 *          negative generic arity -> IAE), structural mutators (changeParameterType/
 *          insert/append/drop/changeReturnType incl. List forms and index edges with
 *          exact IndexOutOfBoundsException messages), hasPrimitives/hasWrappers,
 *          erase/generic/wrap/unwrap round-trips (full 8-primitive wrap matrix,
 *          void<->Void, idempotence), accessors (parameterType/parameterCount/
 *          returnType/parameterList/parameterArray + list-vs-array independence),
 *          equals/hashCode/toString exact forms, and fromMethodDescriptorString/
 *          toMethodDescriptorString round-trip matrix with malformed descriptors ->
 *          IAE ("bad return type"/"bad argument type") / unknown class ->
 *          TypeNotPresentException / null -> NPE.
 * @run main MethodTypeTest
 */

import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testFactories() {
        MethodType mt0 = MethodType.methodType(int.class);
        check(mt0.returnType() == int.class && mt0.parameterCount() == 0,
                "methodType(rtype)");

        MethodType mt1 = MethodType.methodType(int.class, String.class);
        check(mt1.parameterCount() == 1 && mt1.parameterType(0) == String.class,
                "methodType(rtype, ptype0)");

        MethodType mtV = MethodType.methodType(void.class, int.class, long.class);
        check(mtV.returnType() == void.class && mtV.parameterCount() == 2,
                "methodType(void, int, long)");

        MethodType mtVar = MethodType.methodType(int.class, String.class, int.class, boolean.class);
        check(mtVar.parameterCount() == 3, "methodType varargs");

        MethodType mtArr = MethodType.methodType(int.class, new Class<?>[]{String.class, int.class});
        check(mtArr.parameterCount() == 2 && mtArr.parameterType(0) == String.class
                && mtArr.parameterType(1) == int.class, "methodType(array)");

        MethodType mtList = MethodType.methodType(int.class, Arrays.asList(String.class, int.class));
        check(mtList.parameterCount() == 2 && mtList.parameterType(0) == String.class,
                "methodType(List)");

        MethodType mtMT = MethodType.methodType(String.class, MethodType.methodType(int.class, long.class));
        check(mtMT.returnType() == String.class && mtMT.parameterCount() == 1
                && mtMT.parameterType(0) == long.class, "methodType(rtype, MethodType)");

        // equality across array/List factory forms
        check(mtArr.equals(mtList), "array/List factories agree");
        check(mtArr.equals(MethodType.methodType(int.class, String.class, int.class)),
                "varargs factory agrees for same signature");
    }

    static void testGenericMethodType() {
        check(MethodType.genericMethodType(0).parameterCount() == 0
                && MethodType.genericMethodType(0).returnType() == Object.class,
                "genericMethodType(0)");
        check(MethodType.genericMethodType(3).parameterCount() == 3,
                "genericMethodType(3) count");
        for (int i = 0; i < 3; i++) {
            check(MethodType.genericMethodType(3).parameterType(i) == Object.class,
                    "genericMethodType(3) Object param " + i);
        }
        check(MethodType.genericMethodType(3, false).parameterCount() == 3,
                "genericMethodType(3,false) count");
        check(MethodType.genericMethodType(3, true).parameterCount() == 4
                && MethodType.genericMethodType(3, true).parameterType(3) == Object[].class,
                "genericMethodType(3,true) trailing Object[]");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.genericMethodType(-1), "genericMethodType negative IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.genericMethodType(-1, false),
                "genericMethodType(-1,false) IAE");
    }

    static void testFactoryValidation() {
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(null), "methodType null rtype NPE");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(null, int.class), "methodType null rtype NPE 2");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(int.class, (Class<?>) null),
                "methodType null ptype0 NPE");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(int.class, (Class<?>[]) null),
                "methodType null ptype array NPE");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(int.class, (List<Class<?>>) null),
                "methodType null ptype List NPE");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(int.class, new Class<?>[]{String.class, null}),
                "methodType null ptype element NPE");
        checkThrows(NullPointerException.class,
                () -> MethodType.methodType(int.class, Arrays.asList(String.class, null)),
                "methodType null ptype List element NPE");

        // void is not a legal parameter type
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.methodType(int.class, void.class), "void parameter IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.methodType(int.class, void.class, int.class),
                "void parameter varargs IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.methodType(int.class, new Class<?>[]{int.class, Void.TYPE}),
                "void parameter array IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.methodType(int.class, Arrays.asList(int.class, Void.TYPE)),
                "void parameter List IAE");
        // void return is legal
        check(MethodType.methodType(void.class).returnType() == void.class, "void return legal");
    }

    static void testStructuralMutators() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class, boolean.class);

        MethodType changed = mt.changeParameterType(0, Object.class);
        check(changed.parameterType(0) == Object.class
                && changed.parameterType(1) == int.class, "changeParameterType");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.changeParameterType(3, Object.class), "changeParameterType OOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.changeParameterType(-1, Object.class), "changeParameterType negative");
        checkThrows(NullPointerException.class,
                () -> mt.changeParameterType(0, null), "changeParameterType null NPE");

        MethodType insert = mt.insertParameterTypes(0, long.class);
        check(insert.parameterCount() == 4 && insert.parameterType(0) == long.class,
                "insertParameterTypes at 0");
        MethodType insertMid = mt.insertParameterTypes(2, float.class, double.class);
        check(insertMid.parameterCount() == 5 && insertMid.parameterType(2) == float.class
                && insertMid.parameterType(3) == double.class
                && insertMid.parameterType(4) == boolean.class, "insertParameterTypes mid");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.insertParameterTypes(4, int.class), "insertParameterTypes OOB");

        MethodType insertList = mt.insertParameterTypes(1, Arrays.asList(short.class, byte.class));
        check(insertList.parameterCount() == 5 && insertList.parameterType(1) == short.class,
                "insertParameterTypes List");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.insertParameterTypes(-1, Collections.<Class<?>>emptyList()),
                "insertParameterTypes List negative");

        MethodType append = mt.appendParameterTypes(long.class, float.class);
        check(append.parameterCount() == 5 && append.parameterType(3) == long.class
                && append.parameterType(4) == float.class, "appendParameterTypes");
        MethodType appendList = mt.appendParameterTypes(Arrays.asList(short.class));
        check(appendList.parameterCount() == 4 && appendList.parameterType(3) == short.class,
                "appendParameterTypes List");

        MethodType drop = mt.dropParameterTypes(1, 2);
        check(drop.parameterCount() == 2 && drop.parameterType(0) == String.class
                && drop.parameterType(1) == boolean.class, "dropParameterTypes");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(0, 4), "dropParameterTypes too many");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(1, 4), "dropParameterTypes end OOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(-1, 1), "dropParameterTypes negative start");
        checkThrows(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(2, 1), "dropParameterTypes start>end");

        MethodType ret = mt.changeReturnType(void.class);
        check(ret.returnType() == void.class && ret.parameterCount() == 3,
                "changeReturnType");
        checkThrows(NullPointerException.class,
                () -> mt.changeReturnType(null), "changeReturnType null NPE");
    }

    static void testEraseGenericWrapUnwrap() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class);
        MethodType erased = mt.erase();
        check(erased.returnType() == int.class && erased.parameterType(0) == Object.class
                && erased.parameterType(1) == int.class, "erase refs only");

        MethodType generic = mt.generic();
        check(generic.returnType() == Object.class && generic.parameterType(0) == Object.class
                && generic.parameterType(1) == Object.class, "generic all Object");

        MethodType prim = MethodType.methodType(int.class, int.class, long.class);
        MethodType wrapped = prim.wrap();
        check(wrapped.returnType() == Integer.class
                && wrapped.parameterType(0) == Integer.class
                && wrapped.parameterType(1) == Long.class, "wrap primitives");

        MethodType unwrapped = wrapped.unwrap();
        check(unwrapped.equals(prim), "unwrap round-trips to primitives");

        // wrap/unwrap leave reference types alone
        MethodType refs = MethodType.methodType(String.class, Object.class);
        check(refs.wrap().equals(refs) && refs.unwrap().equals(refs),
                "wrap/unwrap no-op on refs");
    }

    static void testHasPrimitivesAndWrappers() {
        check(MethodType.methodType(int.class).hasPrimitives(), "primitive return");
        check(MethodType.methodType(Object.class, int.class).hasPrimitives(), "primitive param");
        check(!MethodType.methodType(Object.class, Object.class).hasPrimitives(),
                "no primitives");
        check(!MethodType.methodType(Object.class, String.class).hasPrimitives(),
                "no primitives ref");

        check(MethodType.methodType(Integer.class).hasWrappers(), "wrapper return");
        check(MethodType.methodType(Object.class, Integer.class).hasWrappers(), "wrapper param");
        check(!MethodType.methodType(Object.class, Object.class).hasWrappers(), "no wrappers");
        check(!MethodType.methodType(int.class, int.class).hasWrappers(), "primitives not wrappers");

        MethodType mixed = MethodType.methodType(int.class, Integer.class);
        check(mixed.hasPrimitives() && mixed.hasWrappers(), "mixed has both");
    }

    static void testAccessors() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class);
        check(mt.returnType() == int.class, "returnType");
        check(mt.parameterCount() == 2, "parameterCount");
        check(mt.parameterType(0) == String.class && mt.parameterType(1) == int.class,
                "parameterType");
        checkThrows(IndexOutOfBoundsException.class, () -> mt.parameterType(2),
                "parameterType OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> mt.parameterType(-1),
                "parameterType negative");

        List<Class<?>> list = mt.parameterList();
        check(list.size() == 2 && list.get(0) == String.class, "parameterList");
        checkThrows(UnsupportedOperationException.class,
                () -> list.add(Object.class), "parameterList immutable");
        checkThrows(UnsupportedOperationException.class,
                () -> list.set(0, Object.class), "parameterList set UOE");

        Class<?>[] arr = mt.parameterArray();
        check(arr.length == 2 && arr[0] == String.class && arr[1] == int.class,
                "parameterArray");
        arr[0] = Object.class;
        check(mt.parameterType(0) == String.class, "parameterArray is a copy");
    }

    static void testEqualsHashCode() {
        MethodType a = MethodType.methodType(int.class, String.class, int.class);
        MethodType b = MethodType.methodType(int.class, String.class, int.class);
        MethodType c = MethodType.methodType(int.class, String.class, long.class);
        MethodType d = MethodType.methodType(long.class, String.class, int.class);
        MethodType e = MethodType.methodType(int.class, String.class);
        check(a.equals(b), "equals same signature");
        check(a.hashCode() == b.hashCode(), "hashCode same");
        check(!a.equals(c), "equals different param");
        check(!a.equals(d), "equals different return");
        check(!a.equals(e), "equals different arity");
        check(!a.equals(null), "equals null");
        check(!a.equals("(String,int)int"), "equals non-MethodType");
    }

    static void testToString() {
        check(MethodType.methodType(int.class).toString().equals("()int"), "toString ()int");
        check(MethodType.methodType(int.class, String.class, int.class).toString()
                .equals("(String,int)int"), "toString (String,int)int");
        check(MethodType.methodType(void.class, int.class).toString().equals("(int)void"),
                "toString (int)void");
        check(MethodType.genericMethodType(2).toString().equals("(Object,Object)Object"),
                "toString generic");
        check(MethodType.genericMethodType(2, true).toString().equals("(Object,Object,Object[])Object"),
                "toString generic trailing array");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++;
                    return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testIndexBoundaryMessages() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class, boolean.class);
        // inserting at index == arity appends at the end (legal)
        check(mt.insertParameterTypes(3, long.class).parameterCount() == 4
                && mt.insertParameterTypes(3, long.class).parameterType(3) == long.class,
                "insertParameterTypes at arity appends");
        // beyond/negative index rejected with the offending index in the message
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.insertParameterTypes(4, long.class), "bad index: 4",
                "insert OOB message");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.insertParameterTypes(-1, long.class), "bad index: -1",
                "insert negative message");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(2, 1), "start=2 end=1", "drop start>end message");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(0, 4), "start=0 end=4", "drop end OOB message");
        // zero-width ranges are no-ops
        check(mt.dropParameterTypes(3, 3).equals(mt), "drop zero-width tail is identity");
        check(mt.dropParameterTypes(1, 1).equals(mt), "drop zero-width middle is identity");
    }

    static void testDescriptorValidationMessages() {
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("(I)", null),
                "bad return type", "missing return descriptor IAE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("(V)I", null),
                "bad argument type", "void argument descriptor IAE");
        // the wording for a non-descriptor string differs across JDK releases
        // (8: "not a method descriptor: bad", 27: "bad signature: bad: not a method type")
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("bad", null),
                "non-descriptor IAE (wording differs across releases)");
        // a well-formed descriptor naming an absent class -> TypeNotPresentException
        try {
            MethodType.fromMethodDescriptorString("()Lno/such/Cls;", null);
            throw new AssertionError("expected TypeNotPresentException");
        } catch (TypeNotPresentException e) {
            check(e.getMessage() != null && e.getMessage().contains("no.such.Cls"),
                    "TypeNotPresentException names missing type");
        }
        // null loader defers to the system/boot loader: java.lang.String resolves
        check(MethodType.fromMethodDescriptorString("(Ljava/lang/String;)V", null)
                .equals(MethodType.methodType(void.class, String.class)),
                "null loader resolves bootstrap classes");
    }

    static void testWrapUnwrapMatrix() {
        Class<?>[] prims = {byte.class, short.class, char.class, int.class, long.class,
                float.class, double.class, boolean.class};
        Class<?>[] wrappers = {Byte.class, Short.class, Character.class, Integer.class,
                Long.class, Float.class, Double.class, Boolean.class};
        for (int i = 0; i < prims.length; i++) {
            MethodType w = MethodType.methodType(prims[i], prims[i]).wrap();
            check(w.returnType() == wrappers[i] && w.parameterType(0) == wrappers[i],
                    "wrap " + prims[i].getName());
            MethodType u = w.unwrap();
            check(u.returnType() == prims[i] && u.parameterType(0) == prims[i],
                    "unwrap " + wrappers[i].getSimpleName());
            check(u.equals(MethodType.methodType(prims[i], prims[i])),
                    "unwrap round-trip " + prims[i].getName());
        }
        // void return wraps to Void and unwraps back to void
        MethodType vw = MethodType.methodType(void.class).wrap();
        check(vw.returnType() == Void.class, "void wraps to Void");
        check(vw.unwrap().returnType() == void.class, "Void unwraps to void");
        // wrapper types are references: wrap leaves them untouched
        check(MethodType.methodType(Integer.class, Long.class).wrap()
                .equals(MethodType.methodType(Integer.class, Long.class)),
                "wrap on wrappers is identity");
        // unwrap actively converts wrappers back to primitives
        check(MethodType.methodType(Integer.class, Long.class).unwrap()
                .equals(MethodType.methodType(int.class, long.class)),
                "unwrap converts wrappers to primitives");
        // non-wrapper references survive unwrap unchanged
        check(MethodType.methodType(String.class, Object.class).unwrap()
                .equals(MethodType.methodType(String.class, Object.class)),
                "unwrap leaves non-wrapper refs");
        check(MethodType.methodType(String.class, Object.class).wrap()
                .equals(MethodType.methodType(String.class, Object.class)),
                "wrap leaves non-wrapper refs");
    }

    static void testEraseGenericDeep() {
        // erase replaces reference types by Object but keeps primitives and void
        MethodType mt = MethodType.methodType(void.class, String.class, int.class);
        MethodType erased = mt.erase();
        check(erased.returnType() == void.class && erased.parameterType(0) == Object.class
                && erased.parameterType(1) == int.class, "erase keeps void and primitives");
        check(MethodType.methodType(int.class, Integer.class).erase().parameterType(0)
                == Object.class, "erase wraps wrappers");
        check(MethodType.methodType(String.class).erase()
                .equals(MethodType.methodType(Object.class)), "erase return type");
        check(MethodType.methodType(int.class).erase()
                .equals(MethodType.methodType(int.class)), "erase primitive identity");
        // generic turns every type into Object, including primitives and void
        MethodType g = MethodType.methodType(int.class, long.class, String.class).generic();
        check(g.returnType() == Object.class && g.parameterType(0) == Object.class
                && g.parameterType(1) == Object.class
                && !g.parameterType(0).isPrimitive() && !g.parameterType(1).isPrimitive(),
                "generic all Object params");
        check(MethodType.methodType(void.class).generic().returnType() == Object.class,
                "generic void return becomes Object");
        // erase/generic are idempotent, and wrap().erase() == generic()
        check(g.erase().equals(g), "erase idempotent on generic");
        check(g.generic().equals(g), "generic idempotent");
        check(MethodType.methodType(int.class, int.class).wrap().erase()
                .equals(MethodType.methodType(int.class, int.class).generic()),
                "wrap then erase equals generic");
    }

    static void testParameterListArrayIndependence() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class);
        check(mt.parameterList().equals(Arrays.asList(mt.parameterArray())),
                "parameterList matches parameterArray content");
        Class<?>[] a1 = mt.parameterArray();
        Class<?>[] a2 = mt.parameterArray();
        check(a1 != a2 && Arrays.equals(a1, a2), "parameterArray returns fresh copies");
        List<Class<?>> l1 = mt.parameterList();
        List<Class<?>> l2 = mt.parameterList();
        check(l1.equals(l2), "parameterList stable across calls");
        a1[0] = Object.class;
        check(mt.parameterType(0) == String.class && mt.parameterArray()[0] == String.class,
                "type unaffected by caller mutation of returned array");
    }

    static void testDescriptorRoundTripMatrix() {
        String[] descs = {"()V", "(I)V", "(IJLjava/lang/String;)Z", "([[D)Ljava/lang/Object;",
                "(Ljava/lang/String;[I)J"};
        for (String d : descs) {
            MethodType back = MethodType.fromMethodDescriptorString(d, null);
            check(back.toMethodDescriptorString().equals(d), "round-trip " + d);
        }
        MethodType varargs = MethodType.genericMethodType(2, true);
        check(varargs.toMethodDescriptorString().equals(
                "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;"),
                "generic varargs descriptor exact");
        check(MethodType.fromMethodDescriptorString(varargs.toMethodDescriptorString(), null)
                .equals(varargs), "generic varargs descriptor round-trip");
    }

    static void testToStringExactForms() {
        check(MethodType.methodType(Object.class, int.class, String.class).toString()
                .equals("(int,String)Object"), "toString (int,String)Object");
        check(MethodType.methodType(void.class, Object[].class).toString()
                .equals("(Object[])void"), "toString array simple name");
        check(MethodType.methodType(char.class, byte.class, double[].class, String[][].class)
                .toString().equals("(byte,double[],String[][])char"),
                "toString nested array params");
        check(MethodType.methodType(boolean.class).toString().equals("()boolean"),
                "toString ()boolean");
        // genericMethodType(2,true): the trailing parameter is a plain Object[] slot
        check(MethodType.genericMethodType(2, true).parameterList().get(2) == Object[].class,
                "genericMethodType(2,true) last param Object[]");
    }

    static void testDescriptorString() {
        MethodType mt = MethodType.methodType(int.class, String.class, int.class);
        String desc = mt.toMethodDescriptorString();
        check(desc.equals("(Ljava/lang/String;I)I"), "toMethodDescriptorString");
        MethodType back = MethodType.fromMethodDescriptorString(desc, null);
        check(back.equals(mt), "descriptor round-trip");

        check(MethodType.fromMethodDescriptorString("()V", null)
                .equals(MethodType.methodType(void.class)), "()V round-trip");
        check(MethodType.fromMethodDescriptorString("([I)Ljava/lang/String;", null)
                .equals(MethodType.methodType(String.class, int[].class)), "([I)String round-trip");
        check(MethodType.fromMethodDescriptorString("(Ljava/lang/String;)I",
                MethodTypeTest.class.getClassLoader())
                .equals(MethodType.methodType(int.class, String.class)),
                "fromMethodDescriptorString with loader");

        checkThrows(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("not a descriptor", null),
                "malformed descriptor IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodType.fromMethodDescriptorString("(I", null),
                "truncated descriptor IAE");
        checkThrows(NullPointerException.class,
                () -> MethodType.fromMethodDescriptorString(null, null),
                "null descriptor NPE");
        check(MethodType.fromMethodDescriptorString("()V", null).toString() != null,
                "parsed method type toString non-null");
    }

    public static void main(String[] args) {
        testFactories();
        testGenericMethodType();
        testFactoryValidation();
        testStructuralMutators();
        testEraseGenericWrapUnwrap();
        testHasPrimitivesAndWrappers();
        testAccessors();
        testEqualsHashCode();
        testToString();
        testDescriptorString();
        testIndexBoundaryMessages();
        testDescriptorValidationMessages();
        testWrapUnwrapMatrix();
        testEraseGenericDeep();
        testParameterListArrayIndependence();
        testDescriptorRoundTripMatrix();
        testToStringExactForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
