# java.beans.PropertyEditorSupport — 设计案（JDK-8）

## 一、API 功能
PropertyEditor 的便利基类（可继承亦可作委托）。
- 构造器：`()`（source=自身）、`(Object source)`（null → NPE）；getSource/setSource。
- 值：setValue（**每次调用都 firePropertyChange**）/getValue；getAsText 默认 `value == null ? null : value.toString()`；setAsText 默认**仅当当前值是 String 时** setValue(text)，否则 `IllegalArgumentException(text)`。
- 默认常量：getJavaInitializationString "???"、getTags null、getCustomEditor null、supportsCustomEditor false、isPaintable false、paintValue 空实现（null Graphics 安全）。
- 事件：add/removePropertyChangeListener（同监听器可重复添加、按次通知）、firePropertyChange（事件 source=getSource()、propertyName/oldValue/newValue 全 null）。

## 二、使用方式
继承并覆写 getAsText/setAsText 等实现自定义编辑器；通过 PropertyEditorManager 注册。

## 三、参考来源
- Oracle Java SE 8 javadoc：java.beans.PropertyEditorSupport（source 语义、事件契约）。
- JavaBeans 规范 1.01-A 第 7 章。
- OpenJDK 回归思路：`test/jdk/java/beans/PropertyEditor/TestEditor.java`（子类化行为）；JDK 8u372 src.zip 的 PropertyEditorSupport.java 逐行核实（Vector 存监听、fire 构造 PropertyChangeEvent(source,null,null,null)）。测试代码自写。

## 四、测试用例设计 + 易错规范细节
覆盖（91 断言）：
1. **testWhiteBox**：implements PropertyEditor、直接继承 Object、**非 Serializable**、15 个公开方法齐全、恰 2 个 public 构造器。
2. **testLegacyBlackBox**：保留旧版全部检查（值/source/paintable/custom/tags/“??? ”/asText/setAsText/IAE/fire 计数）。
3. **testCtorAndSource**：默认 source 即编辑器自身（恒等）；source 构造器恒等；**ctor(null) → NPE**；setSource 可替换、可设回自身、**setSource(null) 不抛**（记录性行为，随后恢复）。
4. **testValueAndTextContract**（易错）：值 null 时 getAsText null 且 setAsText 直接 IAE；值是 String 时 setAsText 存入字符串本身（可连续）；值非 String 时 setAsText 抛 **IllegalArgumentException 且 message 为被拒文本**；任意对象恒等往返 + toString 通道；setAsText(null)（值是 String）→ 值变 null；"???" 与值无关恒定。
5. **testFlagsAndPainting**：五个默认值 + paintValue(null,null) headless 无操作。
6. **testPropertyChangeSemantics**：无监听 fire 无操作；setValue 每次触发（同值/null 值也触发，计数 3）；事件字段：source==getSource()、name/old/new/propagationId 全 null；显式 firePropertyChange；同监听器加两次 → 通知 2 次，删一次 → 1 次，再删 → 0；删 null/未加监听器无操作；监听器列表按实例隔离；**addPropertyChangeListener(null) 不抛**（实现与 javadoc"no action"不符的记录点，随后 remove(null) 清理，不 fire）。
7. **testFireWithNullSource**：setSource(null) 后带监听器 fire → **IllegalArgumentException("null source")**（来自 PropertyChangeEvent 构造器，双侧一致），随后恢复 source。
8. **testSubclassContract**：本地子类覆写 getAsText/setAsText/getTags/getJavaInitializationString/isPaintable，经 PropertyEditor 接口引用走通全链（含继承 setValue 共存、fire 事件 source 为构造器传入 source）。
9. **testInstanceIndependence**：两实例值/source/监听器互不影响。

### 易错规范细节（实证记录）
- 事件模型：PropertyEditorSupport 的 fire **不区分新旧值**（事件 old/new 恒 null、同值也 fire）——与 EnumEditor（真实 old/new、同值不 fire）形成对照；"old/new boxed" 的期望只适用于具体编辑器实现。
- setAsText 默认实现的门槛是"**当前值 instanceof String**"，初值 null 也会被拒——常见误解点。
- addPropertyChangeListener(null) 在 JDK-8 实现为 Vector.addElement(null)（不抛，但会使后续 fire 抛 NPE）；javadoc 声称 no action——**javadoc 与实现分歧**，双侧（8/27 Vector/ArrayList）行为一致：只断言 add 不抛、不断言后续 fire。
- fire 的事件在**有监听器时**才构造：source 为 null 的 IAE 只在至少一个监听器在场时出现（无监听器提前 return）。
- 该类不实现 Serializable（白盒断言），自定义编辑器如需序列化须自行实现。
