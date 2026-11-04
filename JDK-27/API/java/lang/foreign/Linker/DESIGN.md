# Linker — 设计方案

## 1. API 功能
`java.lang.foreign.Linker`（接口）提供本地下行/上行调用。工厂 `nativeLinker()`；`downcallHandle`（两个重载）、`upcallStub`、`defaultLookup()`、`canonicalLayouts()`。

## 2. Java 用户如何使用
```java
Linker linker = Linker.nativeLinker();
MethodHandle mh = linker.downcallHandle(symbol, FunctionDescriptor.of(ValueLayout.JAVA_INT));
Map<String,MemoryLayout> canon = linker.canonicalLayouts();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestDowncall`/`TestUpcall` 是 `nativeLinker()` 的权威用法。
- jextract 用 `canonicalLayouts()` 查 C 类型对应布局；Netty ffm 分支用 `downcallHandle` 封装 native。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 各方法返回类型（MethodHandle/MemorySegment/SymbolLookup/Map）。
黑盒：`nativeLinker()` 非空；`canonicalLayouts()` 含 int/long/byte/double 等 primitive 布局且非空；`defaultLookup()` 非空且找不到不存在符号（不真调 native）。
对应 `LinkerTest.java` 白盒 + 黑盒部分。
