# StackWalker.StackFrame — 设计方案

## 1. API 功能
`StackWalker.StackFrame`（interface）表示一个栈帧：
- 名称：`getClassName/getMethodName/getFileName`
- 位置：`getLineNumber/getByteCodeIndex/isNativeMethod`
- 类型信息：`getDeclaringClass/getDescriptor/getMethodType`
- 转换：`toStackTraceElement()`

## 2. Java 用户如何使用
- 通过 `StackWalker.walk/forEach` 拿到帧后读取调用点信息。
```java
StackWalker.getInstance().forEach(f ->
    System.out.println(f.getClassName() + "." + f.getMethodName() + ":" + f.getLineNumber()));
```

## 3. 开源代码实际用法调研
- 日志框架用 `StackFrame` 提取调用点类名/行号（JDK `test/jdk/java/lang/StackWalker` 系列验证帧字段）。

## 4. 测试用例设计 + 自动化实现
白盒：`StackWalker.StackFrame.class.isInterface()`。
黑盒：首帧 `className/methodName` 为测试类/`main`；`lineNumber >= 0`；`byteCodeIndex >= 0`；`isNativeMethod` false；`toStackTraceElement` 非空；`getDeclaringClass` 为测试类；`getDescriptor/getMethodType` 非空。
对应 `<Simple>Test.java` 黑盒部分。
