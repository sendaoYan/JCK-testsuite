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
