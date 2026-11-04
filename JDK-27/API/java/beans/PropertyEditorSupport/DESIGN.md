# java.beans.PropertyEditorSupport — 设计案

## 1. API 功能
属性编辑器便利基类（implements PropertyEditor）。构造器 `()`/`(source)`；get/setSource、set/getValue、getAsText/setAsText、isPaintable/getJavaInitializationString、add/removePropertyChangeListener、firePropertyChange。

## 2. Java 用户如何使用
继承 PropertyEditorSupport 实现自定义编辑器，重写 getAsText/setAsText 等。

## 3. 开源代码实际用法调研
JDK 源码 `PropertyEditorSupport.java`；大量编辑器继承之。

## 4. 测试用例设计 + 自动化实现
白盒：implements PropertyEditor。黑盒（`testBlackBox`）：默认值、String/非 String 值 asText/setAsText、setAsText 抛 IllegalArgumentException、source 与 firePropertyChange。

## 5. 深化覆盖（批次 beansF，2026-11，91 断言）
参考来源补全：Oracle JDK 27 javadoc；JavaBeans spec 1.01-A 第 7 章；OpenJDK `test/jdk/java/beans/PropertyEditor/TestEditor.java` 回归思路；对照 jdk28 源码核实（监听器容器 Vector→ArrayList，语义不变）。

新增测试方法：testWhiteBox（15 方法面、2 构造器、非 Serializable、直接继承 Object）、testCtorAndSource（默认 source=自身、ctor(null)→NPE、setSource 含 null 接受与恢复）、testValueAndTextContract（null 值拒 setAsText、String 门槛、IAE message=被拒文本、任意对象恒等往返、setAsText(null)→null、"???" 恒定）、testFlagsAndPainting（paintValue(null,null) headless 无操作）、testPropertyChangeSemantics（fire-per-setValue 含同值/null 值、事件五字段（source==getSource、name/old/new/propagationId 全 null）、加两次通知两次删一次删两次、删 null/未加无操作、按实例隔离、**addPropertyChangeListener(null) 不抛**——javadoc 与实现分歧记录点）、testFireWithNullSource（setSource(null)+有监听器 fire → IllegalArgumentException("null source")，双侧一致）、testSubclassContract（子类覆写全套经接口引用走通 + 继承 fire 的 source 为 ctor source）、testInstanceIndependence。

JDK-27 与 JDK-8 行为一致（Vector/ArrayList 差异不可观察、NPE 消息措辞不同不断言），无版本差异断言。
