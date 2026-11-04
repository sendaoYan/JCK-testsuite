# java.io.Serializable 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- **标记接口**：无任何方法/常量/父接口；实现它即声明"本类可被 Java 序列化机制序列化"。
- 契约（由 ObjectOutputStream/ObjectInputStream 解释执行）：
  - 可序列化类可声明 `private static final long serialVersionUID`（版本兼容键）。
  - 可选钩子（非本接口方法）：`writeObject/readObject/readObjectNoData/readResolve/writeReplace`。
  - 子接口：`java.io.Externalizable`（自带 writeExternal/readExternal 契约）。
  - 数组类型、枚举类型自动可序列化。

## 2. Java 用户如何使用
- `class Point implements Serializable { private static final long serialVersionUID = 1L; int x, y; }`。
- `implements Serializable` → `new ObjectOutputStream(bos).writeObject(p)` / `new ObjectInputStream(...).readObject()` 往返。
- 用 `Serializable.class.isAssignableFrom(X.class)` 判断类型能否入流（框架如 Spring/Hibernate/Kryo 常这样探测）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `public class Dummy50 implements Serializable { private static final long serialVersionUID = ...; }` — `jdk25u-dev/test/jdk/java/io/Serializable/concurrentClassDescLookup/ConcurrentClassDescLookup.java:67`（配合 `ObjectStreamClass.lookup(cl).getSerialVersionUID()` 断言 UID）。
- `if (ObjectStreamClass.lookup(cl).getSerialVersionUID() != suid)` — 同文件 :70（以 SUID 为版本键校验序列兼容性）。
- Java 序列化安全加固中常见模式：反序列化前用 `Serializable.class.isAssignableFrom` 过滤目标类型（StackOverflow/GitHub 上的 ObjectInputStream readClassDescriptor + ObjectStreamClass.lookup 方案，见本批 ObjectInputStream 调研）。

## 4. 测试用例设计 + 自动化实现（指向 SerializableTest.java 黑盒 `testBlackBox`）
- 白盒: isInterface、无方法、无常量、无父接口、Externalizable 继承它（`ExternalizableTest.java` 黑盒另测 Externalizable 契约）。
- 黑盒场景:
  1. 资格探测：String/Integer/ArrayList/LocalDate/Enum 为 Serializable；StringBuilder/Thread/Object 不是。
  2. 数组契约：int[]、Object[]、int[][] 均 isAssignableFrom Serializable。
  3. 真实往返：String、int[]（内容逐项断言）、enum 常量（identity 保持）、用户 Point 类、无字段 Empty 类。
  4. 负路径：`writeObject(new Object())` 抛 NotSerializableException 且 message 为类名。
  5. 继承：Serializable 基类的子类自动可序列化且字段往返。
- 健壮性: 全部用内存流，无 IO/全局状态/时序依赖；固定字段值断言精确相等。

产出：`SerializableTest.java`（白盒 6 + 黑盒 ~17 断言）。
