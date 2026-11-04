# java.io.ObjectStreamConstants 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 流头：`STREAM_MAGIC`(0xACED)、`STREAM_VERSION`(5)。
- 类型标签（字节）：`TC_NULL..TC_ENUM`（`TC_BASE..TC_MAX` 区间）。
- 引用句柄：`baseWireHandle`(0x7E0000)。
- 类描述标志：`SC_SERIALIZABLE/SC_WRITE_METHOD/SC_EXTERNALIZABLE/SC_BLOCK_DATA/SC_ENUM`。
- 协议版本：`PROTOCOL_VERSION_1/2`（配 `ObjectOutputStream.useProtocolVersion`）。
- 权限常量：`SUBSTITUTION_PERMISSION/SUBCLASS_IMPLEMENTATION_PERMISSION`。
- 注意：`SERIAL_FILTER_PERMISSION` 为 JDK 9+，本 JDK-8 用例不含。

## 2. Java 用户如何使用
- 自定义 `ObjectOutputStream` 子类改写 `writeStreamHeader` 时需要比较 `STREAM_MAGIC/STREAM_VERSION`；解析原始序列化流的工具按 `TC_*` 标签分派。
- `oos.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1)` 切换旧协议（对 JDK 1.1 兼容场景）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `oout.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);` — `jdk8u-dev/jdk/test/java/io/Externalizable/compatibility/ExternalizableBlockData.java:79`（旧协议块数据兼容往返）。
- 类描述符标志组合校验（SC_* 位）— `jdk8u-dev/jdk/test/java/io/Serializable/classDescFlagConflict/Foo.java`。
- `baseWireHandle` 参与句柄分配 — `jdk8u-dev/jdk/src/share/classes/java/io/ObjectStreamConstants.java`（定义处）。

## 4. 测试用例设计 + 自动化实现（ObjectStreamConstantsTest.java，纯黑盒）
黑盒场景:
1. **真实流头断言**：写出对象后用 DataInputStream 裸读前两个 short == STREAM_MAGIC/STREAM_VERSION（把常量与真实字节流对上）。
2. 全部 TC_* 字节值、TC_BASE/TC_MAX 端点、baseWireHandle、SC_* 位、PROTOCOL_* 精确值断言。
3. 两个权限常量的 getName()（SUBSTITUTION_PERMISSION / SUBCLASS_IMPLEMENTATION_PERMISSION）。
4. useProtocolVersion(V1/V2) 双协议真实往返。
- 健壮性: 纯内存流 + 固定魔数（协议常量跨 JDK 稳定，规范固定值）。
