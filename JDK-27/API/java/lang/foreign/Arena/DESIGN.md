# Arena — 设计方案

## 1. API 功能
`java.lang.foreign.Arena`（接口，继承 `SegmentAllocator` 与 `AutoCloseable`）管理内存段生命周期。工厂 `ofAuto()`/`global()`/`ofConfined()`/`ofShared()`；实例方法 `allocate(long,long)`、`scope()`、`close()`。

## 2. Java 用户如何使用
```java
try (Arena a = Arena.ofConfined()) {
    MemorySegment seg = a.allocate(16, 8);   // 在 close 时统一回收
    seg.set(ValueLayout.JAVA_INT, 0, 42);
}
Arena.global().allocate(8, 1);              // 永不回收
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign` 用 `Arena.ofConfined()`/`ofShared()` 包裹 upcall/downcall 生命周期。
- Netty 5 孵化期 `ffm` 分支用 `Arena.ofConfined()` 分配堆外缓冲；jextract 生成代码用 `Arena` 管理返回段。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `SegmentAllocator`/`AutoCloseable` + 四工厂返回 `Arena` + 实例方法签名。
黑盒：confined 分配/scope 生命周期/close 后 scope 死；global 与 ofAuto 的 close() 抛 UnsupportedOperationException 且 scope 保持 alive；shared 跨线程可访问。
对应 `ArenaTest.java` 白盒 + 黑盒部分。

## 5. 原生调用生命周期深化（ArenaNativeTest）

实证结论（jdk-27.0.1.0.2+1）：
- **close 后段传入 downcall** ⇒ `IllegalStateException`（消息 "Already closed"）；读、写两路均抛；二次 close 也抛 IllegalStateException。
- **confined 段跨线程 downcall** ⇒ `WrongThreadException`；shared 段跨线程 downcall 正常返回。
- **libraryLookup 的 Arena 生命周期**：`libraryLookup(Path, arena)` 打开后 find 正常且符号可真调；arena.close 后 `find`/`findOrThrow` 均抛 `IllegalStateException`（不是 NoSuchElementException）；不存在的库路径 ⇒ `IllegalArgumentException`。
- global/ofAuto 段可正常参与 native 调用；`allocate(size, align)` 对 8/64/256 对齐均兑现（地址取模为 0）。

`ArenaNativeTest.java`（23 断言）：C 写 Java 读 / Java 写 C 读双向交换；confined close 杀死段（读写两路 + 二次 close）；wrong-thread 精确异常；shared 跨线程 native 读；global/ofAuto；对齐兑现；libraryLookup(Path) 真调 + close 后 find/findOrThrow 精确异常 + bogus 路径 IAE。C 源（`jck_read_long`/`jck_write_long`）内嵌运行时编译进 scratch，不入库。

## 5. 契约深化（JDK-27 实测锁定）
- **JDK-27 方法面**：`ofConfined()/ofShared()/ofAuto()/global()`——**无 `of()`**（预览期曾名 of，正式 API 用 ofConfined）。
- **生命周期**：close 后 get/set → ISE；**二次 close → ISE("Already closed")**；try-with-resources 内手动 close 后隐式 close 同样 ISE；global/ofAuto close → UOE。
- **线程归属**：confined 段跨线程 get → **WrongThreadException**（精确类型，非泛化 ISE）；shared 段 worker 写/主线程读可见（4242 闭环）。
- **allocate 契约**：负 size → IAE；alignment 0/非 2 幂 → IAE；(0,1) 合法零长段；新分配段零初始化；maxByteAlignment ≥ 请求对齐且为 2 幂（实测 32/64）。
- allocate(layout)/allocate(layout,n) 委托 SegmentAllocator 默认方法（size 4/12）。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
