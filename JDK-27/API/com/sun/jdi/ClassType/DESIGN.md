# com.sun.jdi.ClassType 行为化测试设计

## 1. API 功能
引用类型与对象：ReferenceType/ClassType/InterfaceType/ArrayType 描述类型，ArrayReference/ObjectReference/StringReference/ClassObjectReference/ClassLoaderReference/ModuleReference 描述对象。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.ClassType extends com.sun.jdi.ReferenceType {`
  - `public static final int INVOKE_SINGLE_THREADED;`
  - `public abstract com.sun.jdi.ClassType superclass();`
  - `public abstract java.util.List<com.sun.jdi.InterfaceType> interfaces();`
  - `public abstract java.util.List<com.sun.jdi.InterfaceType> allInterfaces();`
  - `public abstract java.util.List<com.sun.jdi.ClassType> subclasses();`
  - `public abstract boolean isEnum();`
  - `public abstract void setValue(com.sun.jdi.Field, com.sun.jdi.Value) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException;`
  - `public abstract com.sun.jdi.Value invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List<? extends com.sun.jdi.Value>, int) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;`
  - `public abstract com.sun.jdi.ObjectReference newInstance(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List<? extends com.sun.jdi.Value>, int) throws com.sun.jdi.InvalidTypeException, com.sun.jdi.ClassNotLoadedException, com.sun.jdi.IncompatibleThreadStateException, com.sun.jdi.InvocationException;`
  - `public abstract com.sun.jdi.Method concreteMethodByName(java.lang.String, java.lang.String);`

## 2. Java 用户如何使用
先 `vm.classesByName()` 或 ClassPrepareRequest 拿到 ReferenceType，再 fieldByName/methodsByName/classObject/classLoader；数组用 newInstance 后 get/set 元素。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 ClassesByName.java、ArrayRangeTest.java、ClassLoaderClassesTest.java、AllLineLocations.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 waitPrepared 拿到 Target 类，验证字段/方法/类对象/类加载器/数组读写。

实现见同目录 `ClassTypeTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
