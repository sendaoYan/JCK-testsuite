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
