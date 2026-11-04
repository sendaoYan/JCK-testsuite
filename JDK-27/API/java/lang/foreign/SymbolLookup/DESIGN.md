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

## 5. 真实符号解析深化（SymbolLookupNativeTest）

实证结论（jdk-27.0.1.0.2+1, Linux x64/glibc）：
- **defaultLookup 能直接解析 libc 函数**（strlen 等命中且可真调返回精确值）——Linker javadoc 的权威路径；`libraryLookup("libc")` 抛 IAE，须用精确 soname `libc.so.6`（或平台名链）。
- 符号段 byteSize==0、isNative；同一符号**两次 find 地址相同**，find/findOrThrow 地址一致（非 ASLR 依赖的相等断言）。
- `libraryLookup(Path)` 用自编译 .so 绝对路径打开并真调成功。
- **loaderLookup 生命周期**：System.load 之前看不到符号，load 后可见；同一路径重复 System.load 幂等（不抛）。
- `or()` 跨源组合（defaultLookup ∪ libraryLookup）双向命中；缺失符号 empty。
- `find(null)` ⇒ NPE；findOrThrow 缺失 ⇒ NoSuchElementException；bogus 库名 ⇒ IAE。
- **lookup 随 Arena 死**：close 后 find 抛 IllegalStateException；loaderLookup 不受其它 lookup 的 arena 关闭影响（生命周期挂在 class loader）。

`SymbolLookupNativeTest.java`（25 断言）覆盖上述全部路径，含真实调用验证（strlen "abcde"→5、jck_is_null NULL→1/段→0）。C 源（`jck_is_null`/`jck_read_long`）内嵌运行时编译进 scratch，不入库。
