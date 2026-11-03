# StackTraceElement — 设计方案

## 1. API 功能
`java.lang.StackTraceElement`（final，`Serializable`）表示一个栈帧，两种构造器（4 参/7 参），getter：
`getClassName/getMethodName/getFileName/getLineNumber/isNativeMethod/getModuleName/getModuleVersion/getClassLoaderName`，另 `toString/equals/hashCode`。

## 2. Java 用户如何使用
- 通常由 `Throwable.getStackTrace()` 返回；也可手工构造用于 `Throwable.setStackTrace`。
```java
for (StackTraceElement e : new Throwable().getStackTrace()) {
    System.out.println(e.getClassName() + "." + e.getMethodName() + ":" + e.getLineNumber());
}
```

## 3. 开源代码实际用法调研
- 日志框架解析 `StackTraceElement` 生成调用点；JDK `test/jdk/java/lang/StackTraceElement` 验证 `toString` 格式（`(file:line)`/`(Native Method)`/`(Unknown Source)`）与 `equals/hashCode`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证 final、`Serializable`、两个构造器存在。
黑盒：4 参 getter（模块/加载器信息为 null）；7 参顺序 classLoaderName/moduleName/moduleVersion/className/methodName/fileName/lineNumber；`toString` 三种格式；`isNativeMethod` 对 line -2；值语义 `equals/hashCode`。
对应 `<Simple>Test.java` 黑盒部分。
