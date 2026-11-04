# java.beans.IndexedPropertyChangeEvent 测试设计案

> 覆盖目标：完整契约（构造器逐字段、索引边界含负数、继承关系、序列化 round-trip、
> toString 格式、直接构造后经 PropertyChangeSupport 投递）。headless 安全。
> 本文件为 JDK-8 侧设计案（与 JDK-27 侧同一深化内容）。

## 1. API 功能
- 构造：`IndexedPropertyChangeEvent(Object source, String propertyName, Object oldValue, Object newValue, int index)`。
- 新增访问：`getIndex()`；其余（getSource/getPropertyName/getOldValue/getNewValue/
  setPropagationId/getPropagationId/toString）继承自 PropertyChangeEvent。
- 无公开 setIndex；索引不可变。

## 2. 使用方式
- 索引属性（数组/List 型属性）某下标元素变更时由 `PropertyChangeSupport.fireIndexedPropertyChange`
  构造并广播；也可直接 `new` 后经 `firePropertyChange(PropertyChangeEvent)` 投递。
- 监听器用 `instanceof IndexedPropertyChangeEvent` + `getIndex()` 定位变更下标。

## 3. 参考来源
- Oracle javadoc Java SE 8（`java/beans/IndexedPropertyChangeEvent.html`）。
- JavaBeans 规范 1.01-A（7.4 索引属性、8.4 索引 Bound 属性）。
- OpenJDK `test/jdk/java/beans/PropertyChangeSupport`（索引事件回归思路：等价类为
  下标边界与 old/new 等价性），自写实现。

## 4. 测试用例设计 + 易错规范细节
| 优先级 | 测试点 | 用例方法 | 断言数 |
|---|---|---|---|
| P0 | 构造器逐字段（source/name/old/new/index）+ propagationId 不在构造器（默认 null） | testConstructorContract、testLegacy | 8 |
| P0 | index 边界：0、Integer.MAX_VALUE、-1、Integer.MIN_VALUE 均被接受（**无校验**） | testConstructorContract、testNegativeIndex | 4 |
| P0 | instanceof PropertyChangeEvent / EventObject / Serializable；继承 propagationId set/get | testInheritance | 5 |
| P0 | 序列化 round-trip：具体类保留、index/name/old/new/propagationId 全部存活 | testSerializationRoundTrip | 6 |
| P0 | toString 含 `index=N`（含负数）且以类名开头 | testToString | 5 |
| P0 | 直接构造 + `firePropertyChange(event)`：事件对象**同一性**传递、index 保留、全局+命名监听器都收到、old==new 非 null 不触发 | testFireThroughSupport | 6 |

### 易错规范细节（双侧 JDK-8/JDK-27 实测一致）
- **source 是 transient**（EventObject.source）：序列化 round-trip 后 `getSource()` 为 null；
  其余字段（含 index、propagationId）正常保留。构造事件时 source 允许任意非 null 对象。
- **index 无任何校验**：负数（含 Integer.MIN_VALUE）原样存储。
- `firePropertyChange(PropertyChangeEvent)` 对索引事件无特殊分支：按普通事件过滤
  （old==new 且非 null 不触发），投递的是**调用方构造的同一事件对象**（identity 相等）。
- toString 形如 `IndexedPropertyChangeEvent[propertyName=items; index=-5; oldValue=o;
  newValue=n; propagationId=null; source=s]`——index 字段紧跟 propertyName。
