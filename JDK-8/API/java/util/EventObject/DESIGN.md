# java.util.EventObject 测试设计案（JDK-8）

> 覆盖目标：完整契约——构造 null 拒绝（精确消息）、getSource 同一性、toString 文档格式、
> Object 同一性 equals、Serializable 标记与 **transient source 序列化坑**。

## 1. API 功能

- `EventObject(Object source)`：唯一构造器，source 为 null → `IllegalArgumentException("null source")`。
- `getSource()`：返回构造时传入的源对象（同一实例）。
- `toString()`：文档格式 `getClass().getName() + "[source=" + source + "]"`。
- `implements Serializable`；**source 字段是 transient**（序列化往返后 getSource()==null）。
- 所有事件类的根基类（AWWTEvent/beans PropertyChangeEvent 等继承它）。

## 2. 使用方式

`EventObject e = new EventObject(src); e.getSource(); e.toString();`；
子类化：`class MyEvent extends EventObject { MyEvent(Object s) { super(s); } }`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/EventObject.html`（null source IAE、toString 格式）。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/EventObject.java`：
  `protected transient Object source;`（transient！）、构造器
  `if (source == null) throw new IllegalArgumentException("null source");`、
  toString 为 `getClass().getName() + "[source=" + source + "]"`。

## 4. 测试用例设计（映射到 EventObjectTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getSource 返回构造参数同一实例 | testEventObject |
| P0 | EventObject(null) → IAE，消息精确 "null source" | testNullSource |
| P1 | toString 文档格式逐字（本类与子类各一例）、幂等 | testToStringFormat |
| P1 | getSource 稳定可重复；子类继承 getSource；任意非 null 对象可为源（装箱/数组） | testSourceStability |
| P1 | equals 为 Object 同一性：同 source 两事件不等、自反、≠null/异类型；hashCode 稳定 | testIdentityEquality |
| P1 | Serializable 往返：**getSource()==null**（transient）、还原体 ≠ 原对象、toString 含 "source=null" | testSerializable |
| P0 | 契约深化：null source IAE/Serializable/toString 前缀 | testEventObjectContracts |

## 关键规范细节（易错）

- IAE 消息恒为 `"null source"`（JDK-8 与 JDK-27 实测一致）。
- **source 是 transient**：反序列化后 getSource() 返回 null——需要持久化 source 的事件子类
  必须自定义 writeObject/readObject。这是本类最大的契约坑。
- toString 用**运行时类**名：子类实例打印子类名而非 EventObject。
- equals/hashCode 未覆写：两个包同一 source 的事件互不相等。

## 注意事项

- 确定性：固定 source 对象（String/自定义 toString）；序列化用内存流；无全局副作用。
- headless-safe：不加 `@key`。
