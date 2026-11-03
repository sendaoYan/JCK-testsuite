# MemorySegment — 设计方案

## 1. API 功能
`java.lang.foreign.MemorySegment`（接口）是一段连续内存的视图。查询 `byteSize`/`address`/`isNative`/`isMapped`/`isReadOnly`/`heapBase`/`scope`/`isAccessibleBy`；读写 `get/set/getAtIndex/setAtIndex`（各 primitive + address）；切片 `asSlice`；转换 `asByteBuffer`/`toArray`/`getString`/`setString`/`ofArray`/`ofBuffer`；操作 `fill`/`copy`/`copyFrom`/`mismatch`/`asReadOnly`/`asOverlappingSlice`。

## 2. Java 用户如何使用
```java
try (Arena a = Arena.ofConfined()) {
    MemorySegment seg = a.allocate(16, 8);
    seg.set(ValueLayout.JAVA_INT, 0, 42);
    int v = seg.get(ValueLayout.JAVA_INT, 0);
    MemorySegment slice = seg.asSlice(4, 8);
}
MemorySegment arr = MemorySegment.ofArray(new int[]{1,2,3});
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestByteBuffer`/`TestSegmentCopy` 系统验证 copy/mismatch/切片/读写。
- Netty ffm 分支用 `MemorySegment` 作为 `Buffer` 后端；Panama 教程用 ofArray 访问堆数组。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + NULL + 关键方法签名。
黑盒：NULL 属性；分配 + fill + 多类型 set/get/getAtIndex；asSlice；setString/getString；asReadOnly/asByteBuffer；copy/copyFrom/mismatch/asOverlappingSlice；ofArray + toArray 往返。
对应 `MemorySegmentTest.java` 白盒 + 黑盒部分。
