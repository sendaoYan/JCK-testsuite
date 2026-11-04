# java.io.ObjectInput 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 对象：`readObject()`（抛 ClassNotFoundException + IOException）。
- 字节：`read()`、`read(byte[])`、`read(byte[],int,int)`、`skip(long)`、`available()`。
- 继承自 DataInput：`readFully ×2、skipBytes、readBoolean..readDouble、readChar、readUTF、readLine` 等。
- 生命周期：`close()`（extends AutoCloseable）。

## 2. Java 用户如何使用
- 以接口类型持有输入：`ObjectInput in = new ObjectInputStream(bais); in.readObject(); in.readInt();`——序列化框架/缓存库（如旧版 Ehcache、JGroups 消息编解码）用接口解耦。
- try-with-resources 直接管理（AutoCloseable）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `ObjectInputStream in = new ObjectInputStream(baos); ... in.readObject();`（接口视角的典型形态）— `jdk8u-dev/jdk/test/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181`。
- Externalizable 的 `readExternal(ObjectInput in)` 参数即本接口 — `jdk8u-dev/jdk/src/share/classes/java/io/Externalizable.java`。
- `jdk8u-dev/jdk/test/java/io/Externalizable/compatibility/ExternalizableBlockData.java`：readExternal 内 in.readInt()/readUTF() 组合用法。

## 4. 测试用例设计 + 自动化实现（ObjectInputTest.java，纯黑盒）
黑盒场景:
1. 一条混合流全走接口：对象→对象→readInt→read(byte[])→EOF，顺序与值精确断言。
2. available/skip 在块数据上的接口行为。
3. DataInput 契约：EOF 后 readInt 抛 EOFException。
4. close 走接口（AutoCloseable/TWR）；关后 readObject 抛 IOException。
5. ClassNotFoundException 从 readObject 传播（resolveClass 钩子触发）。
- 健壮性: 内存流固定字节序；无全局状态。

## 深化追加（契约级）

新增维度（`testMethodSetExact`…`testByteLevelContracts`，断言 14 → 49）：
- **方法集精确枚举**：ObjectInput 恰 **22** 个 public 方法（7 自有 + 15 继承自 DataInput）；排序后方法名集合逐一比对；全部 abstract、无 default 方法；`getInterfaces()` 恰为 `[DataInput, AutoCloseable]`；无常量。
- **手写第二实现**（CustomInput/CustomOutput 双实现对照）：纯手写 ObjectInput/ObjectOutput 驱动 Externalizable（ExtPoint, UID 310L）完整往返，全程不经过 OIS/OOS —— 证明接口本身足以承载协议。
- **字节级契约**：`read(null)` → NullPointerException；`close()` 幂等；`readLine()` 按 `\n` 切行（"ab"/"cd"）。
- **ClassNotFoundException 表面**：resolveClass 抛 CNFE → readObject 透传且消息携带流内类名。

负例矩阵：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| OIS.read(null) | NullPointerException | — |
| close 后 readObject | IOException | — |
| 流内类不可解析 | ClassNotFoundException | 流内类二进制名 |

版本差异：无；JDK-8 侧 CustomInput 的 readLine 需 `@SuppressWarnings("deprecation")`，27 侧同。
