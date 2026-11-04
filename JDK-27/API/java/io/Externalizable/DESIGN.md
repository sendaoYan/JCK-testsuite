# java.io.Externalizable 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- `writeExternal(ObjectOutput out)`：由实现者**完全掌控**写出的字节（可调 writeInt/writeUTF/writeLong/writeBoolean 等 DataOutput 全套）。
- `readExternal(ObjectInput in)`：按 writeExternal 的逆序读回，自行恢复对象状态。
- 契约：可外部化类必须有 **public 无参构造器**；反序列化时先调它再调 readExternal；serialVersionUID 同样参与版本匹配；transient 不再由机制忽略——一切由作者手写。

## 2. Java 用户如何使用
- `class Msg implements Externalizable { public Msg() {} int i; public void writeExternal(ObjectOutput out){ out.writeInt(i); } public void readExternal(ObjectInput in){ i = in.readInt(); } }`。
- `oos.writeObject(msg)` → 机制写入类描述 + 调用 writeExternal；`ois.readObject()` → 无参构造 + readExternal。
- 与 Serializable 对比：Serializable 自动写全部非 transient 字段；Externalizable 精确手写、流更小、更快，但要求无参构造器。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `class Foo implements Externalizable { ... public void writeExternal(ObjectOutput out) throws IOException { out.writeInt(x); } ... }` — `jdk25u-dev/test/jdk/java/io/Serializable/classDescFlagConflict/Foo.java`（Externalizable 流标志 SC_EXTERNALIZABLE 冲突测试）。
- `jdk25u-dev/test/jdk/java/io/Serializable/classDescHooks/ExternLoopback.java`：writeExternal/readExternal 往返（loopback）用法。
- `jdk25u-dev/test/jdk/java/io/Serializable/InvalidClassException/noargctor/DefaultPackage.java`：无 public 无参构造器 → 反序列化 InvalidClassException("no valid constructor") 的权威触发用例。

## 4. 测试用例设计 + 自动化实现（指向 ExternalizableTest.java 黑盒 `testBlackBox`）
- 白盒: 接口、extends Serializable、两个抽象方法、异常签名、无常量。
- 黑盒场景:
  1. 手写表单完整往返（int/UTF/long/boolean），含"作者故意写 transient 字段"证明机制不干预。
  2. 反序列化确实调用 public 无参构造器（静态计数器）。
  3. 负路径：缺无参构造器 → InvalidClassException("no valid constructor")（message 稳定，精确断言）。
  4. 组合：对象后紧跟原始数据（writeInt）→ readObject 后 readInt 恢复，验证读写对称。
  5. 以 ObjectOutput 视角多态使用。
- 健壮性: 内存流、固定字段值、无时序/全局状态。

产出：`ExternalizableTest.java`（白盒 6 + 黑盒 ~12 断言）。
