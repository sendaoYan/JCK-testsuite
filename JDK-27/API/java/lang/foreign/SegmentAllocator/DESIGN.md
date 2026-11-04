# SegmentAllocator — 设计方案

## 1. API 功能
`java.lang.foreign.SegmentAllocator`（接口）统一内存分配抽象。工厂 `slicingAllocator(MemorySegment)`/`prefixAllocator(MemorySegment)`；default `allocate(long)/allocate(MemoryLayout)/allocate(MemoryLayout,long)` 与 `allocateFrom(...)`（标量/字符串/数组）；抽象 `allocate(long,long)`。

## 2. Java 用户如何使用
```java
SegmentAllocator alloc = SegmentAllocator.slicingAllocator(pool);
MemorySegment s = alloc.allocate(ValueLayout.JAVA_INT);
MemorySegment str = alloc.allocateFrom("hello");
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestSegmentAllocators` 验证 slicing/prefix 分配行为。
- jextract 生成代码用 `SegmentAllocator.allocateFrom` 创建 Java 字符串对应的 C 段。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + allocate/allocateFrom/工厂返回类型。
黑盒：slicing/prefix 分配尺寸与可写性；`allocateFrom` 标量/字符串/变长数组；`allocate(long)`；Arena 作为 allocator 的多态分配。
对应 `SegmentAllocatorTest.java` 白盒 + 黑盒部分。
