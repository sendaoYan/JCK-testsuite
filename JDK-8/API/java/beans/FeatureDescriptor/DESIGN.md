# java.beans.FeatureDescriptor 测试设计案

> 覆盖目标：完整契约（默认态、displayName/shortDescription 回落规则、值表含 NPE 矩阵、
> attributeNames 精确键集、toString、identity equals、非序列化）。headless 安全。

## 一、API 功能
- 特性描述符基类（BeanDescriptor/PropertyDescriptor/EventSetDescriptor/MethodDescriptor 的共同父类）。
- name/displayName/shortDescription 三级名称；expert/hidden/preferred 三个标志。
- 通用命名属性表：setValue/getValue/attributeNames（内部 Hashtable）。
- toString 输出 `java.beans.FeatureDescriptor[name=...; values={k=v; ...}]` 形式。

## 二、使用方式
- 由 Introspector 生成或手动构造，工具（IDE/编辑器生成器）读取展示。
- 名称回落规则：displayName 未设 → 返回 name；shortDescription 未设 → 返回 displayName。
  一旦显式设置即固定；setShortDescription(null) 后又回落到 displayName。

## 三、参考来源
- Oracle javadoc Java SE 8（`java/beans/FeatureDescriptor.html`）。
- JavaBeans 规范 1.01-A 第 5/8/9/11 节（properties/events/methods 设计时信息）。
- OpenJDK `jdk/src/share/classes/java/beans/FeatureDescriptor.java`（Hashtable 语义、回落实现）；
  回归思路：Introspector 众多回归依赖 setValue("transient") 等属性表行为。

## 四、测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 默认态：name/displayName/shortDescription 全 null、三标志 false、attributeNames 空枚举 | testDefaultState |
| P0 | 回落规则：setName→displayName/shortDescription 跟随；逐级覆盖；shortDescription(null) 回落 | testDisplayNameFallThrough |
| P0 | setValue/getValue：String/Integer/Boolean 值、覆盖写、未知键 null、空串键合法 | testValues |
| P0 | NPE 矩阵：setValue(null,·)、setValue(k,null)、getValue(null) → NullPointerException（Hashtable 语义，null 值不可存） | testValueNegatives |
| P1 | attributeNames：恰好等于已设键集合（按 set 比较）；加第 4 键后计数 4 | testAttributeNames |
| P1 | setName(null) 合法（无异常，name 变 null） | testSetNameNull |
| P1 | 三标志 true/false 往返 | testFlagToggles |
| P1 | toString 非空、含 name、含 `k=v` 值项 | testToString |
| P1 | equals 未覆写：同状态两实例不相等（identity 语义）、hashCode 实例内稳定 | testEqualsIdentity |
| P1 | **非 Serializable**：isAssignableFrom false + ObjectOutputStream.writeObject 抛 NotSerializableException | testNotSerializable |
| P0 | 白盒：非接口、public 声明方法恰为 16 个访问器/变址器、无 equals 覆写、直接继承 Object | testWhiteBox |

## 易错规范细节（实测，JDK-8 与 JDK-27 一致）
- **FeatureDescriptor 家族全部不可序列化**（javadoc 未声明 Serializable；ObjectStreamClass.lookup 返回 null）。
  任何"序列化 round-trip"应断言 NotSerializableException。
- setValue 的值不能为 null（Hashtable.put(key,null) 抛 NPE）——值表无法区分"未设"与"设为 null"。
- attributeNames 返回键的 Enumeration，顺序为 Hashtable 散列序（非插入序），必须按集合比较。
- JDK-27 的 NPE 带 helpful message（如 "Cannot invoke Object.hashCode() because key is null"），
  JDK-8 message 为 null——只断言异常类，不断言 message。
