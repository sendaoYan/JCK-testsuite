# com.sun.jdi.ReferenceType 行为化测试设计

## 1. API 功能
引用类型与对象：ReferenceType/ClassType/InterfaceType/ArrayType 描述类型，ArrayReference/ObjectReference/StringReference/ClassObjectReference/ClassLoaderReference/ModuleReference 描述对象。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.ReferenceType extends com.sun.jdi.Type, java.lang.Comparable<com.sun.jdi.ReferenceType>, com.sun.jdi.Accessible {`
  - `public abstract java.lang.String name();`
  - `public abstract java.lang.String genericSignature();`
  - `public abstract com.sun.jdi.ClassLoaderReference classLoader();`
  - `public default com.sun.jdi.ModuleReference module();`
  - `public abstract java.lang.String sourceName() throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<java.lang.String> sourceNames(java.lang.String) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<java.lang.String> sourcePaths(java.lang.String) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.lang.String sourceDebugExtension() throws com.sun.jdi.AbsentInformationException;`
  - `public abstract boolean isStatic();`
  - `public abstract boolean isAbstract();`
  - `public abstract boolean isFinal();`
  - `public abstract boolean isPrepared();`
  - `public abstract boolean isVerified();`
  - `public abstract boolean isInitialized();`
  - `public abstract boolean failedToInitialize();`
  - `public abstract java.util.List<com.sun.jdi.Field> fields();`
  - `public abstract java.util.List<com.sun.jdi.Field> visibleFields();`
  - `public abstract java.util.List<com.sun.jdi.Field> allFields();`
  - `public abstract com.sun.jdi.Field fieldByName(java.lang.String);`
  - `public abstract java.util.List<com.sun.jdi.Method> methods();`
  - `public abstract java.util.List<com.sun.jdi.Method> visibleMethods();`
  - `public abstract java.util.List<com.sun.jdi.Method> allMethods();`
  - `public abstract java.util.List<com.sun.jdi.Method> methodsByName(java.lang.String);`
  - `public abstract java.util.List<com.sun.jdi.Method> methodsByName(java.lang.String, java.lang.String);`
  - `public abstract java.util.List<com.sun.jdi.ReferenceType> nestedTypes();`
  - `public abstract com.sun.jdi.Value getValue(com.sun.jdi.Field);`
  - `public abstract java.util.Map<com.sun.jdi.Field, com.sun.jdi.Value> getValues(java.util.List<? extends com.sun.jdi.Field>);`
  - `public abstract com.sun.jdi.ClassObjectReference classObject();`
  - `public abstract java.util.List<com.sun.jdi.Location> allLineLocations() throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<com.sun.jdi.Location> allLineLocations(java.lang.String, java.lang.String) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<com.sun.jdi.Location> locationsOfLine(int) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<com.sun.jdi.Location> locationsOfLine(java.lang.String, java.lang.String, int) throws com.sun.jdi.AbsentInformationException;`
  - `public abstract java.util.List<java.lang.String> availableStrata();`
  - `public abstract java.lang.String defaultStratum();`
  - `public abstract java.util.List<com.sun.jdi.ObjectReference> instances(long);`
  - `public abstract boolean equals(java.lang.Object);`
  - `public abstract int hashCode();`
  - `public abstract int majorVersion();`
  - `public abstract int minorVersion();`
  - `public abstract int constantPoolCount();`
  - `public abstract byte[] constantPool();`

## 2. Java 用户如何使用
先 `vm.classesByName()` 或 ClassPrepareRequest 拿到 ReferenceType，再 fieldByName/methodsByName/classObject/classLoader；数组用 newInstance 后 get/set 元素。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 ClassesByName.java、ArrayRangeTest.java、ClassLoaderClassesTest.java、AllLineLocations.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 waitPrepared 拿到 Target 类，验证字段/方法/类对象/类加载器/数组读写。

实现见同目录 `ReferenceTypeTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
