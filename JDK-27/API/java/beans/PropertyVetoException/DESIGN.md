# java.beans.PropertyVetoException 测试设计案

> 覆盖目标：完整契约（继承 Exception、message、getPropertyChangeEvent 身份、null 事件、throw/catch）。

## 1. API 功能
- 构造：`PropertyVetoException(String mess, PropertyChangeEvent evt)`。
- 访问：`getPropertyChangeEvent()`。

## 2. 使用方式
- 约束属性 setter 或 `VetoableChangeSupport` 监听器抛出，阻止赋值。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyVetoException.html`）。
- JCK `api/java_beans/PropertyVetoException`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | extends Exception、message、getPropertyChangeEvent 身份 | testBasics |
| P0 | null message / null event 保留 | testBasics |
| P1 | throw/catch 往返 | testThrowCatch |

## 关键规范细节
- `getPropertyChangeEvent()` 返回构造时传入的同一对象（含 null）。
- 该异常是受检异常（extends Exception），调用方必须捕获或声明。

---

## 追加覆盖（契约深化，JDK-8 与 JDK-27 行为一致，均为实测）

### 新增测试方法
| 方法 | 覆盖维度 |
|---|---|
| testInventory | 受检定型（非 RuntimeException/Error）；Serializable；serialVersionUID=129596057694162164L（双侧一致）；唯一构造器 (String, PropertyChangeEvent)；声明公开 getPropertyChangeEvent |
| testMessageContract | message 精确；空串；getLocalizedMessage==getMessage；toString 精确格式；fillInStackTrace 返回 this；getStackTrace 非空 |
| testCause | 默认 cause null；initCause 返回 this 且身份存储；二次 initCause → IllegalStateException；initCause(null) 合法 |
| testSerialization | round-trip：message 与 **event 字段都保留**（name/old/new/propagationId 逐字段）；event.source 为 null（EventObject.source 是 transient，双侧实测一致）；initCause 设置的 cause 也保留 |
| testRealVetoCycle | 真实 veto 周期：VCS 监听器抛出 → 调用方收到**同一对象**；message/事件字段可读；可作 cause 身份挂接；printStackTrace 有输出；getSuppressed 默认空 |

### 新增易错细节（实测）
- 序列化后 `getPropertyChangeEvent()` 非 null——event 字段参与序列化（非 transient），
  但其中的 `source` 因 EventObject 的 transient 声明而丢失为 null。
- 二次 initCause 抛 IllegalStateException（Throwable 通用契约）。
