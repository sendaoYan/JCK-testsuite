# com.sun.jdi.FloatValue 行为化测试设计

## 1. API 功能
镜像值的根与原始值：Mirror/Value/Type/PrimitiveValue 及 8 个原始 Value 接口 + VoidValue，代表调试目标里的值/类型。

公开成员（JDK-27 javap）：
  - `public interface com.sun.jdi.FloatValue extends com.sun.jdi.PrimitiveValue, java.lang.Comparable<com.sun.jdi.FloatValue> {`
  - `public abstract float value();`
  - `public abstract boolean equals(java.lang.Object);`
  - `public abstract int hashCode();`

## 2. Java 用户如何使用
调试器用 `vm.mirrorOf(int/boolean/.../String)` 或 `mirrorOfVoid()` 拿到 Value，再读 `.value()`/`.type()`/`.name()`/`.signature()` 与原始类型转换。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 DataDumpTest.java、EvalArgs.java、GetUninitializedStringValue.java 通过 mirrorOf 造值。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用 launchTarget 拿真 VM，mirrorOf 各种值并断言 value/type/signature/equals/hashCode/compareTo 及原始转换。

实现见同目录 `FloatValueTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。
