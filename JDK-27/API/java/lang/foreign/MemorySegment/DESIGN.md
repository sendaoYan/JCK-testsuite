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

## 5. 契约深化（JDK-27 实测锁定）
- **越界矩阵（全 IOOBE）**：INT@size-3、负 offset、getAtIndex(-1)、setAtIndex 越界、asSlice(9)/asSlice(-1)。
- **对齐仍在强制**：get/set(JAVA_LONG, offset 4) → **IAE**（非 IOOBE）；JAVA_LONG_UNALIGNED@4 合法；heap byte[] 段 INT@1 → IAE（堆基址 1 字节对齐）。
- **只读违规是 IAE**（27 实测，非 UOE）：set/fill/copyFrom/setString 对 asReadOnly 视图全 IAE；读不受限；asReadOnly 幂等（equals 成立但非同实例）。
- **equals 语义**：base+offset+size 视图同一性——同内容不同地址 **false**；asSlice(0) equals 原段且 hashCode 相同。
- **NULL 契约**：address 0、size 0、isNative true、scope 常活、任意线程可访问、内容访问 IOOBE、ofAddress(0) 与其 equals。
- **字符串**：allocateFrom("中")=3 UTF-8 字节+NUL（size 4）；空串=NUL 1 字节；**无 NUL 终止的 getString → IOOBE**。
- **堆段**：int[] 段 asByteBuffer → UOE（仅 byte[] 段可转）；elements/spliterator 按给定元素布局计数（16B/INT=4）。
- copy 窗口外字节不动；typed copy 按布局往返。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
