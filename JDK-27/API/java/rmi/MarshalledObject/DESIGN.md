# java.rmi.MarshalledObject 测试设计案

> 覆盖目标：完整契约（final + Serializable、构造序列化、get 深拷贝隔离、null、equals/hashCode）。

## 1. API 功能
- 构造：`MarshalledObject(T obj) throws IOException`。
- 读：`get() throws IOException, ClassNotFoundException`；`hashCode`/`equals`。

## 2. 使用方式
- 序列化一个可远程传递的对象为字节包裹；`get()` 反序列化得到等价副本。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/rmi/MarshalledObject.html`）。
- OpenJDK `test/jdk/java/rmi/MarshalledObject`；JCK `api/java_rmi/MarshalledObject`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | final + Serializable | testStructure |
| P0 | String round-trip、equals self | testStringRoundTrip |
| P0 | get 深拷贝隔离（改返回副本不影响内容） | testCopyIsolation |
| P0 | null 载荷；非可序列化载荷→IOException | testNullAndNonSerializable |
| P1 | equals/hashCode（内容相等、不同、null、非本类） | testEqualsHashCode |

## 关键规范细节
- `get()` 每次返回**新的深拷贝**，修改返回对象不影响包裹内容。
- 非 Serializable 载荷在构造时抛 `NotSerializableException`（IOException 子类）。
- null 载荷合法，`get()` 返回 null；两个 null 载荷相等。
