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
