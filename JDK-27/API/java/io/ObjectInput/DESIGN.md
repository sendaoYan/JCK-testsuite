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
- `ObjectInputStream in = new ObjectInputStream(baos); ... in.readObject();`（接口视角的典型形态）— `jdk25u-dev/test/jdk/java/io/Serializable/InvalidClassException/noargctor/Serialize/SubclassAcrossPackage.java:181`。
- Externalizable 的 `readExternal(ObjectInput in)` 参数即本接口 — `jdk25u-dev/src/java.base/share/classes/java/io/Externalizable.java`（每个 Externalizable 实现都通过 ObjectInput 读取）。
- `jdk25u-dev/test/jdk/java/io/Externalizable/compatibility/ExternalizableBlockData.java`：readExternal 内 in.readInt()/readUTF() 组合用法。

## 4. 测试用例设计 + 自动化实现（指向 ObjectInputTest.java 黑盒 `testBlackBox`）
- 白盒: 接口、extends DataInput+AutoCloseable、方法/重载面、全抽象。
- 黑盒场景:
  1. 一条混合流全走接口：对象→对象→readInt→read(byte[])→EOF，顺序与值精确断言。
  2. available/skip 在块数据上的接口行为。
  3. DataInput 契约：EOF 后 readInt 抛 EOFException。
  4. close 走接口（AutoCloseable/TWR）；关后 readObject 抛 IOException。
  5. ClassNotFoundException 从 readObject 传播（resolveClass 钩子触发）。
- 健壮性: 内存流固定字节序；无全局状态。

产出：`ObjectInputTest.java`（白盒 ~30 + 黑盒 ~15 断言）。
