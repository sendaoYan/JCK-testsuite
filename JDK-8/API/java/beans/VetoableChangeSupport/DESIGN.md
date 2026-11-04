# java.beans.VetoableChangeSupport 测试设计案

> 覆盖目标：完整契约（null source→NPE、no-op、全局 vs 命名、fire/no-fire 条件、veto 中止投递）。

## 1. API 功能
- 构造：`VetoableChangeSupport(Object sourceBean)`。
- 注册：`addVetoableChangeListener`/`removeVetoableChangeListener`/`getVetoableChangeListeners`（全局/命名）。
- 触发：`fireVetoableChange`（Object/int/boolean/PropertyChangeEvent），受检 `PropertyVetoException`。
- 查询：`hasListeners(String)`。

## 2. 使用方式
- 约束属性 setter 先 `fireVetoableChange`，若监听器抛出 `PropertyVetoException` 则放弃赋值。

## 3. 参考来源
- Java SE 8/27 javadoc（`java/beans/VetoableChangeSupport.html`）。
- OpenJDK `test/jdk/java/beans/VetoableChangeSupport`；JCK `api/java_beans/VetoableChangeSupport`。

## 4. 测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | null source→NPE；null 监听器/名 no-op；getListeners(null) 空 | testConstructorAndNoOp |
| P0 | 全局 vs 命名 + hasListeners 语义 | testGlobalVsNamed |
| P0 | Object/int/boolean/event fire 精确条件 | testFireConditions |
| P0 | veto 监听器抛 PropertyVetoException 并中止后续监听器 | testVetoPropagation |

## 关键规范细节
- 与 PropertyChangeSupport 相同：`hasListeners(name)` 计入全局监听器；Object 触发条件 `old==null||new==null||!old.equals(new)`；int/boolean 仅不同时触发。
- 任一监听器抛出 `PropertyVetoException` 会中止对后续监听器的投递并把异常上抛。

## 5. 深化补充（round A，双侧同源，30→68 断言）
新增用例方法（保留原有全部用例）：
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | **veto 自动回滚**：监听器顺序 [A, vetoer, C]——A 先收正事件（old→new），vetoer 抛 PropertyVetoException 后，A 再收**新构造的**回滚事件（new→old，值互换）；vetoer 自身不参与回滚（恰好 1 次调用）；C 完全不通知；上抛的 PropertyVetoException 携带**原始事件**（未互换） | testVetoRollback |
| P0 | 对回滚事件再 veto：被忽略，仍上抛最初 vetoer 的异常 | testRollbackVetoIgnored |
| P0 | fireVetoableChange(event) 重载：每个监听器收到**同一事件对象**（identity）；pve.getPropertyChangeEvent() == 调用方传入的事件 | testEventOverloadIdentity |
| P0 | 监听器抛 RuntimeException：原样上抛（不包装成 PropertyVetoException）、**无回滚**（先前监听器只收 1 次）、后续监听器跳过 | testListenerRuntimeException |
| P1 | fireVetoableChange((PropertyChangeEvent) null) → NPE | testNullEventFire |
| P0 | 序列化 round-trip：可序列化监听器（全局+命名）存活且仍触发；非可序列化监听器（全局与命名）静默丢弃；非可序列化 source → NotSerializableException | testSerializationRoundTrip |
| P1 | VetoableChangeListenerProxy 细节：命名监听器包装成 proxy（带属性名）；getVetoableChangeListeners(name) 返回原对象 identity；hasListeners(null) 只计全局 | testProxyDetails |
| P1 | 重复 remove / null / 未注册监听器 → no-op | testRemoveNoOps |
| P1 | 返回数组为防御性副本 | testDefensiveArrays |
| P1 | 命名监听器只收到本属性 fire；全局全部收到 | testNamedFireScope |
| P2 | toString 非 null 且含类名 | testToStringMisc |

### 易错规范细节（JDK-8 与 JDK-27 实测一致）
- **回滚（rollback）语义**：veto 时按「已通知监听器」倒序逐一发送 old/new 互换的**新**事件；
  回滚事件本身被 veto 时异常被吞掉；上抛给调用方的仍是 vetoer 的原始 PropertyVetoException。
- RuntimeException 不走回滚路径（catch 只针对 PropertyVetoException）。
- 序列化行为与 PropertyChangeSupport 同构：只保留 Serializable 监听器，source 非 Serializable
  则整个序列化失败（NotSerializableException）。
- 注：**clearVetoableChangeListeners 并不存在**（javadoc/源码均无此 API，JDK-8 与 JDK-27 一致）；
  「清空监听器」只能逐个 remove。
