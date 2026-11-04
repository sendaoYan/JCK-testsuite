# java.beans.ParameterDescriptor 测试设计案

> 覆盖目标：公共面恰为默认构造器 + 完整继承 FeatureDescriptor 行为（默认态/回落/标志/
> 值表 NPE 矩阵/attributeNames/非序列化），并验证其在 MethodDescriptor 内的使用。headless 安全。

## 一、API 功能
- 方法参数描述符（extends FeatureDescriptor）：**公开 API 仅一个默认构造器**，
  无任何自有公开方法/字段（状态全部继承）。

## 二、使用方式
- `new MethodDescriptor(m, new ParameterDescriptor[]{ pd0, pd1 })` 为参数命名/描述；
  Introspector 不自动生成参数描述（默认 getParameterDescriptors 为 null）。

## 三、参考来源
- Oracle javadoc Java SE 8（`java/beans/ParameterDescriptor.html`）。
- JavaBeans 规范 1.01-A 第 8.6.3 节。
- OpenJDK `ParameterDescriptor.java`（空壳子类，仅默认构造器 + 包私有 dup 构造器）。

## 四、测试用例设计
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 公共面：恰 1 个公共构造器（无参）；自有 public 方法数 0；声明字段数 0 | testWhiteBox |
| P0 | 继承默认态：name/displayName/shortDescription null、三标志 false、attributeNames 空 | testDefaultState |
| P0 | 继承回落规则（name→displayName→shortDescription）与覆盖/null 回落 | testFallThroughRules |
| P0 | 继承值表：round-trip/覆盖/未知键 + null 键/值 NPE 矩阵 | testValues/testValueNegatives |
| P1 | 三标志往返；attributeNames 恰为所设键 | testFlags/testAttributeNames |
| P1 | 嵌入 MethodDescriptor：数组长度/元素身份/名字可见 | testInsideMethodDescriptor |
| P1 | toString 非空、identity equals、非 Serializable → NotSerializableException | testToStringAndEquals/testNotSerializable |

## 易错规范细节（实测，JDK-8 与 JDK-27 一致）
- ParameterDescriptor 是"空壳"子类：反射可见 getDeclaredFields()==0、getDeclaredMethods() 无 public 项
  （源码只有默认构造器与包私有拷贝构造器）——公共面断言据此设计。
- 值表继承 Hashtable 语义：null 键/null 值/null 查询均 NPE。
- 非序列化家族成员（断言 NotSerializableException 而非 round-trip）。
