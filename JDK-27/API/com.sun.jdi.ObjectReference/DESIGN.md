# com.sun.jdi.ObjectReference 行为化测试设计

## 1. API 功能
引用类型与对象：ReferenceType/ClassType/InterfaceType/ArrayType 描述类型，ArrayReference/ObjectReference/StringReference/ClassObjectReference/ClassLoaderReference/ModuleReference 描述对象。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.ObjectReference extends com.sun.jdi.Value {`
  - `public static final int INVOKE_SINGLE_THREADED;`
  - `public static final int INVOKE_NONVIRTUAL;`
  - `public abstract com.sun.jdi.ReferenceType referenceType();`
  - `public abstract com.sun.jdi.Value getValue(com.sun.jdi.Field);`
  - `public abstract java.util.Map<com.sun.jdi.Field, com.sun.jdi.Value> getValues(java.util.List<? extends com.sun.jdi.Field>);`
  - `public abstract void setValue(com.sun.jdi.Field, com.sun.jdi.Value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;`
  - `public abstract com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List<? extends com.sun.jdi.Value>, int) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;`
  - `public abstract void disableCollection();`
  - `public abstract void enableCollection();`
  - `public abstract boolean isCollected();`
  - `public abstract long uniqueID();`
  - `public abstract java.util.List<com.sun.jdi.ThreadReference> waitingThreads() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract com.sun.jdi.ThreadReference owningThread() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract int entryCount() throws com.sun.jdi.IncompatibleThreadStateException;`
  - `public abstract java.util.List<com.sun.jdi.ObjectReference> referringObjects(long);`
  - `public abstract boolean equals(java.lang.Object);`
  - `public abstract int hashCode();`

## 2. Java 用户如何使用
先 `vm.classesByName()` 或 ClassPrepareRequest 拿到 ReferenceType，再 fieldByName/methodsByName/classObject/classLoader；数组用 newInstance 后 get/set 元素。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 ClassesByName.java、ArrayRangeTest.java、ClassLoaderClassesTest.java、AllLineLocations.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 waitPrepared 拿到 Target 类，验证字段/方法/类对象/类加载器/数组读写。

实现见同目录 `ObjectReferenceTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
