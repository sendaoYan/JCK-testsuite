# Linker.Option — 设计方案

## 1. API 功能
`java.lang.foreign.Linker.Option`（接口）是下行/上行调用的选项标记。静态工厂 `firstVariadicArg(int)`、`captureCallState(String...)`、`captureStateLayout()`、`critical(boolean)`。

## 2. Java 用户如何使用
```java
Linker.Option critical = Linker.Option.critical(true);
MethodHandle mh = linker.downcallHandle(sym, fd, critical);
StructLayout state = Linker.Option.captureStateLayout();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestDowncall` 用 `Linker.Option.firstVariadicArg`/`captureCallState`/`critical` 测 C 变参与 errno 捕获。
- jextract 对 `critical` 函数生成 `Linker.Option.critical(true)` 参数。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 四静态工厂返回类型（Option/StructLayout）。
黑盒：各工厂非空；`captureStateLayout()` 有成员且 size 非负；`critical(true/false)` 互异；`firstVariadicArg(0)` 相等稳定。
对应 `OptionTest.java` 白盒 + 黑盒部分。
