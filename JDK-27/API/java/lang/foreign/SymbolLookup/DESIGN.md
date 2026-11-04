# SymbolLookup — 设计方案

## 1. API 功能
`java.lang.foreign.SymbolLookup`（接口）按名字查找 native 符号。`find(String)` 返回 `Optional<MemorySegment>`；default `findOrThrow(String)`、`or(SymbolLookup)`；静态 `loaderLookup()`、`libraryLookup(String|Path, Arena)`。

## 2. Java 用户如何使用
```java
SymbolLookup libc = SymbolLookup.libraryLookup("libc.so.6", arena);
MemorySegment malloc = libc.find("malloc").orElseThrow();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestSymbolLookup` 验证 loader/library lookup 与 or 组合。
- jextract 用 `SymbolLookup.loaderLookup()` 找导出符号；Panama 教程用 `libraryLookup` 调 libc。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + find/findOrThrow/or + 三个静态工厂返回类型。
黑盒：loaderLookup 找不到不存在符号、findOrThrow 抛 NoSuchElementException；or() 组合合成 lookup；bogus library 抛 IAE；在常见 Unix libc 名上尽力查 malloc 存在性（符号段 size 0、scope alive）。
对应 `SymbolLookupTest.java` 白盒 + 黑盒部分。
