# java.lang.StackTraceElement 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个栈帧读字段」的冒烟。重点补：白盒结构（final、
> Serializable、仅 4 参构造器、JDK-8 无 getModuleName/getClassLoaderName/getModuleVersion）、
> toString 三种格式（`(file:line)`/`(Native Method)`/`(Unknown Source)`）、isNativeMethod 对 -2 的行号语义、
> 值语义 equals/hashCode、序列化往返保持值。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **构造器**：`StackTraceElement(String className, String methodName, String fileName, int lineNumber)`
  （JDK-8 仅此一个；7 参模块/类加载器构造器是 Java 9+）。
- **getter**：`getClassName`/`getMethodName`/`getFileName`/`getLineNumber`/`isNativeMethod`。
- **对象**：`toString`/`equals`/`hashCode`（值语义）；实现 `Serializable`。

## 2. Java 用户如何使用

- 通常由 `Throwable.getStackTrace()` 返回；也可手工构造用于 `Throwable.setStackTrace`。
- 日志框架解析 `StackTraceElement` 生成调用点；`isNativeMethod()` 判断行号为 -2 的 native 帧。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/StackTraceElement.html`）：4 参构造器、toString 格式、
  `isNativeMethod`（lineNumber == -2）。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/StackTraceElement.java`：
  final、Serializable、toString 拼装逻辑。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/StackTraceElement`。

## 4. 测试用例设计（映射到 StackTraceElementTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Serializable、4 参构造器存在、7 参构造器与 getModuleName/getClassLoaderName 在 JDK-8 不存在 | testWhiteBox |
| P0 | 4 参 getter + isNativeMethod（line 42 非 native） | testGetters |
| P1 | toString 三种格式（file:line / Native Method / Unknown Source）+ line -2 → native | testToString |
| P1 | 值语义 equals/hashCode（同字段相等，行号不同不等） | testEqualsHashCode |
| P1 | 序列化往返保持 equals/hashCode | testSerialization |

## 关键规范细节（易错，已锁入断言）

- **`isNativeMethod()` 当且仅当 `lineNumber == -2`**；`toString` 对应 `C.m(Native Method)`。
- **`toString` 三种格式**：`fileName != null && lineNumber >= 0` → `C.m(F.java:42)`；
  `lineNumber == -2` → `C.m(Native Method)`；否则 → `C.m(Unknown Source)`。
- **JDK-8 无 7 参构造器，也无 `getModuleName`/`getModuleVersion`/`getClassLoaderName`**（Java 9+ 才引入）。
- **equals/hashCode 是值语义**：四个字段全相等即相等。

## 注意事项

- 序列化断言用同一字段构造，避免栈内容不确定性；headless 安全，不加 `@key`。
