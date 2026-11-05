/*
 * @test
 * @summary java.lang.Cloneable conformance: white-box structure + black-box behavior
 *          (Object.clone() throws CloneNotSupportedException without Cloneable, shallow copy
 *          with it).
 * @run main CloneableTest
 */

public class CloneableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Cloneable");
        check(c.isInterface(), "Cloneable is an interface");
        check(c.getMethods().length == 0 && c.getDeclaredMethods().length == 0,
              "Cloneable is a marker interface (no methods)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class NotCloneable {
        public Object clone() throws CloneNotSupportedException { return super.clone(); }
    }

    static class CloneableBox implements Cloneable {
        int v;
        CloneableBox(int v) { this.v = v; }
        public Object clone() throws CloneNotSupportedException { return super.clone(); }
    }

    static void testNotCloneable() throws Exception {
        NotCloneable n = new NotCloneable();
        try {
            n.clone();
            throw new AssertionError("clone() without Cloneable: expected CloneNotSupportedException");
        } catch (CloneNotSupportedException expected) {
            passed++;
        }
    }

    static void testCloneable() throws Exception {
        CloneableBox b = new CloneableBox(42);
        CloneableBox c = (CloneableBox) b.clone();
        check(c != b, "clone returns a distinct instance");
        check(c.getClass() == b.getClass(), "clone has the same class");
        check(c.v == b.v, "clone copies field values (shallow)");
    }

    // ==================== 契约深化（2026-11）====================

    /**
     * 不覆写 clone()：在类体内直接调用从 Object 继承的 protected clone()
     * （protected 成员在同文件嵌套类的类体内可达，无需反射）。
     */
    static class Shallow implements Cloneable {
        int a;
        String s;
        int[] arr;
        static int ctorCount = 0;
        Shallow(int a, String s, int[] arr) { this.a = a; this.s = s; this.arr = arr; ctorCount++; }
        Shallow invokeInheritedClone() throws CloneNotSupportedException {
            return (Shallow) this.clone();
        }
    }

    /** 未实现 Cloneable 的类在类体内直调继承的 clone() → CloneNotSupportedException。 */
    static class NoClone {
        NoClone invokeInheritedClone() throws CloneNotSupportedException {
            return (NoClone) this.clone();
        }
    }

    /** super.clone() 按运行时类分配：父类定义 clone()，子类不覆写也得到子类实例。 */
    static class Parent implements Cloneable {
        int v;
        Parent(int v) { this.v = v; }
        public Parent clone() {
            try { return (Parent) super.clone(); }
            catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
    }

    static class Child extends Parent {
        int w;
        Child(int v, int w) { super(v); this.w = w; }
    }

    /** 覆写 equals/hashCode 的可克隆类：clone 与原对象值相等但不同一。 */
    static class ValueClone implements Cloneable {
        final int x;
        ValueClone(int x) { this.x = x; }
        public ValueClone clone() {
            try { return (ValueClone) super.clone(); }
            catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
        public boolean equals(Object o) {
            return o instanceof ValueClone && ((ValueClone) o).x == x;
        }
        public int hashCode() { return 31 * x + 7; }
    }

    /** 深拷贝对照：覆写 clone 复制数组 → 引用独立。 */
    static class DeepCopy implements Cloneable {
        int[] data;
        DeepCopy(int[] data) { this.data = data; }
        public DeepCopy clone() {
            try {
                DeepCopy c = (DeepCopy) super.clone();
                c.data = data.clone();
                return c;
            } catch (CloneNotSupportedException e) { throw new AssertionError(e); }
        }
    }

    static class SubBox extends CloneableBox {
        SubBox(int v) { super(v); }
    }

    /** 白盒补充：0 字段 0 类型参数；Object.clone 的 protected/native/异常签名。 */
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.lang.Cloneable");
        check(c.getDeclaredMethods().length == 0, "Cloneable declares no methods");
        check(c.getMethods().length == 0, "Cloneable exposes no public methods");
        check(c.getDeclaredFields().length == 0, "Cloneable declares no fields");
        check(c.getTypeParameters().length == 0, "Cloneable is not generic");
        java.lang.reflect.Method clone = Object.class.getDeclaredMethod("clone");
        check(java.lang.reflect.Modifier.isProtected(clone.getModifiers()),
              "Object.clone() is protected");
        check(java.lang.reflect.Modifier.isNative(clone.getModifiers()),
              "Object.clone() is native (JVM intrinsic)");
        check(clone.getExceptionTypes().length == 1
                  && clone.getExceptionTypes()[0] == CloneNotSupportedException.class,
              "Object.clone() declares CloneNotSupportedException");
    }

    /** 浅拷贝招牌闭环：不覆写 clone 直接拿继承实现（同文件嵌套类体内直调）。 */
    static void testShallowCloneWithoutOverride() throws Exception {
        Shallow.ctorCount = 0;
        Shallow orig = new Shallow(7, "tag", new int[] { 1, 2, 3 });
        check(Shallow.ctorCount == 1, "constructor ran once for the original");
        Shallow copy = orig.invokeInheritedClone();
        check(copy != orig, "clone() != original (distinct instance)");
        check(copy.getClass() == orig.getClass(), "clone preserves runtime class");
        check(copy.a == 7, "primitive field copied by value");
        check(copy.s == orig.s, "reference field shares the same String (shallow)");
        check(copy.arr == orig.arr, "array field shares the SAME array object (shallow copy)");
        copy.arr[0] = 99;
        check(orig.arr[0] == 99, "mutating clone.arr[0] is visible through original.arr[0]");
        check(java.util.Arrays.equals(orig.arr, new int[] { 99, 2, 3 }), "shared array content");
        copy.a = 1000;
        check(orig.a == 7, "primitive change in clone does NOT affect original");
        check(Shallow.ctorCount == 1, "clone() did NOT invoke the constructor");
        check(copy instanceof Cloneable, "clone result still implements Cloneable");
        check(copy instanceof Shallow, "clone result is a Shallow");
        check(copy != orig.invokeInheritedClone(), "each clone() call returns a new instance");
        Shallow withNull = new Shallow(0, null, null);
        Shallow nullCopy = withNull.invokeInheritedClone();
        check(nullCopy.s == null && nullCopy.arr == null, "null reference fields stay null");
        check(nullCopy.a == 0, "zero primitive stays 0");
    }

    /** 未实现 Cloneable 的类调 clone → CloneNotSupportedException 真触发 + 消息锁定。 */
    static void testNotCloneableDirect() throws Exception {
        NoClone n = new NoClone();
        try {
            n.invokeInheritedClone();
            throw new AssertionError("expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
            check(e.getClass() == CloneNotSupportedException.class,
                  "exactly java.lang.CloneNotSupportedException (no subclass)");
            check(e instanceof Exception, "CloneNotSupportedException is checked (extends Exception)");
            check(NoClone.class.getName().equals(e.getMessage()),
                  "Object.clone() message is the cloned class name: " + e.getMessage());
        }
    }

    /** super.clone() 按运行时类分配：父类的 clone() 返回子类实例。 */
    static void testSuperCloneReturnsRuntimeClass() {
        Child ch = new Child(11, 22);
        Parent p = ch.clone();
        check(p instanceof Child, "Parent.clone() on a Child returns a Child instance");
        check(p.getClass() == Child.class, "runtime class of the clone is Child");
        check(p.v == 11, "parent field v copied");
        check(((Child) p).w == 22, "child field w copied");
        check(p != ch, "clone is a distinct instance");
        check(Child.class.getSuperclass() == Parent.class, "Child extends Parent");
        check(ch instanceof Cloneable, "Cloneable-ness inherited from Parent");
    }

    /** 覆写 equals 的可克隆类：clone.equals(orig) 且 hashCode 相等。 */
    static void testCloneEqualsContract() {
        ValueClone orig = new ValueClone(5);
        ValueClone copy = orig.clone();
        check(copy != orig, "distinct instances");
        check(copy.equals(orig), "clone equals original under value semantics");
        check(copy.hashCode() == orig.hashCode(), "equal clones share hashCode");
        check(!copy.equals(new ValueClone(6)), "value difference breaks equals");
    }

    /** 深拷贝对照：覆写 clone 复制数组 → 引用独立、互改不可见。 */
    static void testDeepCopyContrast() {
        DeepCopy orig = new DeepCopy(new int[] { 4, 5 });
        DeepCopy copy = orig.clone();
        check(copy != orig, "deep clone distinct instance");
        check(copy.data != orig.data, "deep copy allocates a NEW array (contrast to shallow)");
        check(java.util.Arrays.equals(copy.data, orig.data), "array contents equal");
        copy.data[0] = 77;
        check(orig.data[0] == 4, "deep copy isolates mutations");
    }

    /** 继承链：子类直接继承父类 public clone() 仍可用。 */
    static void testInheritedPublicClone() throws Exception {
        SubBox sb = new SubBox(123);
        Object o = sb.clone();
        check(o instanceof SubBox, "inherited public clone returns SubBox instance");
        check(((SubBox) o).v == 123, "field copied through inherited clone");
        check(o.getClass() == SubBox.class, "runtime class preserved");
    }

    /** 数组天生可克隆：int[].clone() 不经 Cloneable 接口检查。 */
    static void testArraysAreAlwaysCloneable() {
        int[] src = { 1, 2, 3 };
        int[] copy = src.clone();
        check(copy != src, "array clone is a new array");
        check(java.util.Arrays.equals(copy, src), "array clone contents equal");
        check(Cloneable.class.isAssignableFrom(int[].class), "array types implement Cloneable");
        String[] sCopy = new String[] { "a" }.clone();
        check(sCopy.length == 1 && "a".equals(sCopy[0]), "reference array clone contents equal");
    }

    /** 标记接口的现身处：实现类的 getInterfaces 含 Cloneable；常见类型的事实。 */
    static void testMarkerFacts() {
        boolean found = false;
        for (Class<?> i : Shallow.class.getInterfaces()) {
            if (i == Cloneable.class) found = true;
        }
        check(found, "Shallow.class.getInterfaces() lists Cloneable");
        check(new java.util.ArrayList<Object>() instanceof Cloneable, "ArrayList implements Cloneable");
        check(!Cloneable.class.isAssignableFrom(String.class), "String is NOT Cloneable (immutable by design)");
        check(!Cloneable.class.isAssignableFrom(Object.class), "Object itself does not implement Cloneable");
        check(Exception.class.isAssignableFrom(CloneNotSupportedException.class),
              "CloneNotSupportedException is-a Exception");
    }

    /** 可变嵌套对象字段同样浅共享（不只数组）。 */
    static class Inner {
        int z;
        Inner(int z) { this.z = z; }
    }

    static class HoldsInner implements Cloneable {
        Inner in;
        HoldsInner(Inner in) { this.in = in; }
        HoldsInner invokeInheritedClone() throws CloneNotSupportedException {
            return (HoldsInner) this.clone();
        }
    }

    static class SubNoClone extends NoClone {
        SubNoClone() { super(); }
    }

    /** 拷贝共享语义补充：嵌套对象引用共享、两次克隆三实例、构造器零调用。 */
    static void testSharedNestedObjectAndMultiClone() throws Exception {
        HoldsInner orig = new HoldsInner(new Inner(3));
        HoldsInner c1 = orig.invokeInheritedClone();
        HoldsInner c2 = orig.invokeInheritedClone();
        check(c1 != orig && c2 != orig && c1 != c2, "two clones are pairwise distinct");
        check(c1.in == orig.in && c2.in == orig.in, "nested object reference shared by all clones");
        c1.in.z = 42;
        check(orig.in.z == 42 && c2.in.z == 42, "nested mutation visible through every alias");
        check(orig.in.getClass() == Inner.class, "nested field runtime class unchanged");
        Object asObject = orig.invokeInheritedClone();
        check(asObject instanceof HoldsInner, "inherited clone() result castable to owning class");
    }

    /** 未实现 Cloneable 的可继承性事实：其子类同样不可克隆。 */
    static void testSubOfNonCloneable() throws Exception {
        check(!Cloneable.class.isAssignableFrom(SubNoClone.class),
              "subclass of non-Cloneable is still not Cloneable");
        try {
            SubNoClone s = new SubNoClone();
            s.invokeInheritedClone();
            throw new AssertionError("expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
            check(SubNoClone.class.getName().equals(e.getMessage()),
                  "message names the actual runtime class of the clone target");
        }
    }

    /** 库类型的 Cloneable 事实表。 */
    static void testLibraryCloneableFacts() {
        check(Cloneable.class.isAssignableFrom(java.util.Date.class), "java.util.Date is Cloneable");
        check(Cloneable.class.isAssignableFrom(java.util.ArrayList.class), "ArrayList is Cloneable");
        check(Cloneable.class.isAssignableFrom(java.util.HashMap.class), "HashMap is Cloneable");
        check(!Cloneable.class.isAssignableFrom(StringBuilder.class),
              "StringBuilder is NOT Cloneable (uses copy constructors)");
        check(Cloneable.class.isAssignableFrom(Object[].class), "Object[] is Cloneable");
        check(Cloneable.class.isAssignableFrom(byte[].class), "byte[] is Cloneable");
        java.util.Date d = new java.util.Date(1000L);
        java.util.Date dc = (java.util.Date) d.clone();
        check(d != dc && d.getTime() == dc.getTime(), "Date.clone round-trip value equal");
        check(dc.getClass() == java.util.Date.class, "Date.clone preserves runtime class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxShape();
        testNotCloneable();
        testCloneable();
        testShallowCloneWithoutOverride();
        testNotCloneableDirect();
        testSuperCloneReturnsRuntimeClass();
        testCloneEqualsContract();
        testDeepCopyContrast();
        testInheritedPublicClone();
        testArraysAreAlwaysCloneable();
        testMarkerFacts();
        testSharedNestedObjectAndMultiClone();
        testSubOfNonCloneable();
        testLibraryCloneableFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
