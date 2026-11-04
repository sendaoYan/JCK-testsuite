# java.beans.PropertyChangeEvent 测试设计案

> 覆盖目标：完整契约（构造器 + null source→IAE、四个 getter、propagationId、toString）。

## 1. API 功能
- 构造：`PropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue)`。
- 访问：`getSource`/`getPropertyName`/`getOldValue`/`getNewValue`；`setPropagationId`/`getPropagationId`；`toString`。

## 2. 使用方式
- 由 `PropertyChangeSupport` 构造并投递给监听器；监听器读取 old/new 值判断变化。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyChangeEvent.html`）。
- OpenJDK `test/jdk/java/beans/PropertyChangeEvent`；JCK `api/java_beans/PropertyChangeEvent`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器 + source 身份 + 四个 getter | testConstructor |
| P0 | null source→IllegalArgumentException；null name/old/new 允许 | testConstructor |
| P1 | propagationId 默认 null、set/get、set null | testPropagationId |
| P1 | toString 含 name/old/new/propagationId/source | testToString |

## 关键规范细节
- 构造器委托 `EventObject(source)`，null source 抛 `IllegalArgumentException`。
- toString 形如 `PropertyChangeEvent[propertyName=p; oldValue=old; newValue=new; propagationId=...; source=...]`。

## 5. 深化补充（round A，双侧同源，14→40 断言）
新增用例方法（保留原有全部用例）：
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 装箱类型：Integer/Boolean/Long 作为 old/new 原样保存 | testBoxedValues |
| P0 | 序列化 round-trip：propertyName/oldValue/newValue/propagationId 全部存活；**source transient → null**；old 值非可序列化 → NotSerializableException | testSerializationRoundTrip |
| P0 | null name 事件可经 PropertyChangeSupport 投递（name 为 null 送达全局监听器） | testNullNameDelivery |
| P1 | equals/hashCode 未覆写：同字段两个事件**不**相等（Object.equals 身份语义）、自反、hashCode/toString 稳定 | testEqualsHashCode |
| P1 | 不支持 clone：非 Cloneable、无公开 clone 方法（getMethod("clone") → NoSuchMethodException） | testCloneNotSupported |
| P1 | toString 全 null 字段渲染 "null" 且以类名开头 | testToStringNulls |
| P1 | 继承矩阵：instanceof EventObject/Serializable | testInheritance |

### 易错规范细节（JDK-8 与 JDK-27 实测一致）
- EventObject.source 为 **transient**：事件序列化 round-trip 后 getSource() 返回 null；
  name/old/new/propagationId 正常保留。
- 事件对非可序列化 old/new 值**不做**跳过处理（与 PCS 对监听器的静默丢弃不同）：序列化直接抛
  NotSerializableException。
- propagationId 不在构造器中（默认 null），需事后 setPropagationId；无 getter 之外的访问途径。
- equals/hashCode 未覆写（身份语义），不可把「字段相同」的两个事件视为相等。
