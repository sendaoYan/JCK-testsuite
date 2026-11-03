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
