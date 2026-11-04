# java.beans.PropertyChangeSupport 测试设计案

> 覆盖目标：完整契约（null source→NPE、null 监听器/属性名 no-op、全局 vs 命名监听器、
> 精确 fire/no-fire 条件、索引 fire、remove 停投、防御性数组）。headless 安全。

## 1. API 功能
- 构造：`PropertyChangeSupport(Object sourceBean)`。
- 注册：`addPropertyChangeListener`（全局/命名）、`removePropertyChangeListener`、`getPropertyChangeListeners`（两种）。
- 触发：`firePropertyChange`（Object/int/boolean/PropertyChangeEvent）、`fireIndexedPropertyChange`（Object/int/boolean）。
- 查询：`hasListeners(String)`。

## 2. 使用方式
- bean 内部持有 `PropertyChangeSupport`，setter 修改后调用 `firePropertyChange`。
- 命名监听器只接收对应属性名的事件；全局监听器接收全部。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/PropertyChangeSupport.html`）。
- OpenJDK `test/jdk/java/beans/PropertyChangeSupport`；JCK `api/java_beans/PropertyChangeSupport`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | null source→NPE；null 监听器/null 名 no-op；getListeners(null) 空 | testConstructorAndNoOp |
| P0 | 全局 vs 命名（命名包成 proxy）；事件 source/属性名 | testGlobalVsNamed |
| P0 | Object fire 精确条件（相等非 null 不触发、双 null 触发） | testFireConditions |
| P0 | int/boolean fire 仅不同时触发；装箱值 | testFireConditions |
| P0 | firePropertyChange(事件) 条件；索引 fire + IndexedPropertyChangeEvent.getIndex() | testFireEvent、testIndexedFire |
| P1 | remove 停投；防御性数组 | testGlobalVsNamed、testDefensiveArrays |

## 关键规范细节
- `hasListeners(name)` 在存在**全局监听器**时对任意 name 返回 true（全局监听器计入）。
- `firePropertyChange(String,Object,Object)` 条件：`old==null || new==null || !old.equals(new)` 才触发——**两个 null 会触发**，相等非 null 不触发。
- int/boolean 重载只在 `old != new` 时触发。
- `addPropertyChangeListener(name, listener)` 当 name 或 listener 为 null 时是 **no-op**（不会退化成全局注册）。
- 命名监听器内部以 `PropertyChangeListenerProxy` 包装；`getPropertyChangeListeners()` 返回全局 + proxy 混合数组。

## 5. 深化补充（round A，双侧同源，42→85 断言）
新增用例方法（保留原有全部用例）：
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 序列化 round-trip：可序列化监听器（全局+命名）以**副本**存活并仍能触发；非可序列化监听器被**静默丢弃**；source 随之序列化（事件 getSource 为值相等副本）；非可序列化 source → NotSerializableException | testSerializationRoundTrip |
| P0 | 同一监听器注册两次 → 每次 fire 通知两次；remove 一次只注销一个注册；命名重复同理 | testDuplicateRegistration |
| P0 | 通知顺序：全局监听器按注册顺序；**混合时先全部全局（注册序）再命名**（双侧实测一致） | testNotificationOrder |
| P0 | 监听器抛 RuntimeException：原样上抛调用方、**后续监听器不通知**、移除抛异常者后恢复投递 | testListenerException |
| P0 | fire 判定按 equals 而非 ==：同一引用但 equals()==false 仍触发；同一引用且 equals 相同不触发 | testIdentityVsEqualsFire |
| P1 | firePropertyChange(null, a, b)：全局监听器收到 name 为 null 的事件；命名监听器收不到 | testNullNameFire |
| P1 | firePropertyChange((PropertyChangeEvent) null) → NullPointerException | testNullEventFire |
| P1 | hasListeners(null)：仅命名注册时 false；有全局注册时 true（只计全局） | testHasListenersNull |
| P1 | proxy 细节：命名监听器包成 PropertyChangeListenerProxy（带属性名）；getPropertyChangeListeners(name) 返回**未包装原对象**（identity） | testProxyDetails |
| P1 | remove 无操作矩阵：未注册/null 监听器/null 名 一律 no-op | testRemoveNoOps |
| P1 | fireIndexedPropertyChange 送达该属性命名监听器（IndexedPropertyChangeEvent 类型 + index 保留） | testIndexedFireNamed |
| P2 | toString 非 null 且含类名 | testToStringMisc |

### 易错规范细节（JDK-8 与 JDK-27 实测一致）
- PCS 序列化只保留 Serializable 监听器，其余静默丢弃（不抛异常）；round-trip 后原监听器实例
  不会再被通知（通知发到反序列化出的副本上）。
- source 字段参与序列化：source 非 Serializable 时整个 PCS 序列化抛 NotSerializableException。
- RuntimeException 无回滚概念（与 VetoableChangeSupport 不同）：异常穿透到调用方，本轮后续监听器跳过。
- 触发条件用 `old.equals(new)` 判等：覆盖 equals 恒 false 的对象（同一引用）也会触发。
