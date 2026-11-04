# java.io.ObjectOutput 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 对象：`writeObject(Object)`。
- 字节：`write(int)`、`write(byte[])`、`write(byte[],int,int)`。
- 继承自 DataOutput：`writeBoolean..writeDouble、writeChar、writeUTF、writeBytes、writeChars`。
- 生命周期：`flush()`、`close()`（extends AutoCloseable）。

## 2. Java 用户如何使用
- `ObjectOutput out = new ObjectOutputStream(bos); out.writeObject(bean); out.writeInt(x); out.flush();`——序列化框架以接口持有输出。
- Externalizable 实现的 `writeExternal(ObjectOutput out)` 内组合 writeInt/writeUTF 写自己的表单。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectOutputStream out = new ObjectOutputStream(baos); out.writeObject(...)` — `jdk8u-dev/jdk/test/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181-212`。
- Externalizable 的 `writeExternal(ObjectOutput out)` 参数即本接口 — `jdk8u-dev/jdk/src/share/classes/java/io/Externalizable.java`。
- `out.writeInt(x); out.writeUTF(s);` 组合 — `jdk8u-dev/jdk/test/java/io/Externalizable/compatibility/ExternalizableBlockData.java`。

## 4. 测试用例设计 + 自动化实现（ObjectOutputTest.java，纯黑盒）
黑盒场景:
1. **接口全表面单流回归**：writeObject(Bean)+writeObject(null)+全套原始类型+writeUTF+writeBytes/writeChars+write(byte[])+write(byte[],off,len) → 对端按序逐项读回，最后 read()==-1（编解码对称性）。
2. 独立 null 对象往返（TC_NULL）。
3. close() 传播到底层流；对"关闭后拒绝写入"的底层流，writeObject 抛 IOException（用 Strict 包装流验证传播链）。
4. flush 幂等；实现类 ObjectOutputStream 同时是 Closeable+Flushable。
- 健壮性: 固定字节序（DataOutput 大端）跨平台一致；内存流。

## 深化追加（契约级）

新增维度（`testMethodSetExact`…`testOosByteContracts`，断言 17 → 48）：
- **方法集精确枚举**：ObjectOutput 恰 **17** 个 public 方法（4 自有 + 13 继承自 DataOutput）；排序名集合逐一比对；全部 abstract、无 default；`getInterfaces()` 恰 `[DataOutput, AutoCloseable]`；无常量。
- **手写第二实现**：CustomOutput 记录 writeObject 捕获队列 + DataOutputStream 字节；writeInt(0x01020304) 字节序为**大端** 01 02 03 04；writeShort 0x0506 → 05 06。
- **Externalizable 双实现往返**（ExtRec, UID 410L）：writeObject(null) 经接口引用捕获为 null。
- **OOS 块分帧字节契约**：裸 `write(0x1234)` 输出 `header(4) + 0x77(TC_BLOCKDATA) + 0x01(长度) + 0x34(截断到低 8 位)` 共 7 字节。

负例矩阵：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| OOS.write((byte[])null) | NullPointerException | — |
| close 后 writeObject | IOException | 底层 `Stream closed`（本测试用 Strict 流断言类型） |

版本差异：无。
