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

## 5. 原生互操作深化（MemorySegmentNativeTest）

实证结论（jdk-27.0.1.0.2+1）：
- **只读段写入的精确异常**：`ro.set(...)` 抛 `IllegalArgumentException`（"Attempt to write a read-only segment"），**不是** UnsupportedOperationException；而 C 侧通过只读段**读**完全合法。
- C 返回的裸指针段 byteSize==0；`strchr` 命中 ⇒ 非空段且相对同段偏移可断言（非 ASLR 依赖），未命中 ⇒ `equals(MemorySegment.NULL)`。
- `MemorySegment.NULL` 传给 C 即 NULL 指针（`jck_is_null`==1），address()==0、byteSize()==0。
- 堆段（`ofArray(long[])`/`ofArray(byte[])`）作 ADDRESS 实参 ⇒ `IllegalArgumentException`。
- C 的指针算术以 Java 传入的段基址为准：`asSlice(8)` 传给 C 只影响偏移 8 处；`getAtIndex/setAtIndex` 与 C `p[i]` 一致。

`MemorySegmentNativeTest.java`（27 断言）：Long.MIN/MAX 双向往返；asSlice 偏移语义（写偏移 8 不动偏移 0）；索引访问与 C p[i] 一致 + setAtIndex 对 C 可见；只读段 C 可读/Java 写 IAE；NULL 段三元组；堆段两种数组 IAE；C 字符串互操作（NUL 终结符物化、strlen 一致、strchr 偏移/NULL）。C 源（`jck_read_long`/`jck_write_long`/`jck_read_int_at`/`jck_is_null`）内嵌运行时编译进 scratch，不入库。
