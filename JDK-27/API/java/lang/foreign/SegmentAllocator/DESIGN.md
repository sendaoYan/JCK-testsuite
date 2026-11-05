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

## 5. 契约深化（JDK-27 实测锁定）
- **allocateFrom 家族（27 名称，非 allocateArray）**：`allocateFrom(OfInt, 1,2,3)` size 12 + 值表回读；空 varargs → 零长段；long/float 边界值保真。
- **allocateFrom(String) NUL 终止**："abc"=4 字节、'中'=4 字节（3+1）；显式 Charset 变体生效。
- **slicingAllocator**：窗口互异、单调前进、界内；写入落在池段对应偏移。
- **prefixAllocator**：每次分配都返回同一窗口（addr==池基址），后写覆盖前写。
- allocate(struct) 尊重布局对齐（%8==0）且 size==布局 size；typed copy 堆数组段往返。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
