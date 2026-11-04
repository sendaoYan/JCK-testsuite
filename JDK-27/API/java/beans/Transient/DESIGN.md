# java.beans.Transient — 设计案

## 1. API 功能
注解（@Target METHOD/FIELD）。`boolean value()` 默认 true，标记属性为 transient（不参与内省）。

## 2. Java 用户如何使用
`@Transient` 标注 getter 或字段，让 Introspector 忽略该属性。

## 3. 开源代码实际用法调研
JDK 源码 `Transient.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射验证 value 默认 true 与 @Transient(false)。

---

# 深化补充（2026-11-05，batch beansC）

> **语义修正**：上文「标记属性为 transient（不参与内省）」表述不准确。实测（JDK-8 与 JDK-27 一致）：`@Transient` **不会**让属性从 `getPropertyDescriptors()` 中消失，而是让对应 `PropertyDescriptor` 带上键 `"transient"` 的值属性（Boolean 包装类型）。另注：Target 实测恰为 `{METHOD}`，不含 FIELD；`value()` 默认值实测为 **true**。

## 一、新增覆盖
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 结构：RUNTIME retention；Target 恰为 {METHOD}；value() 原始 boolean、默认 true、单元素；非 Documented | testWhiteBox |
| P0 | 注解读取：true/false 值、未标注方法 null、代理 equals、isAnnotationPresent | testAnnotationValues |
| P0 | 行为：@Transient(true) getter → 属性存在 + `getValue("transient")==Boolean.TRUE` + 读写方法/类型不变；@Transient(false) → FALSE；仅 setter 标注同样生效；未标注 → 值属性缺失 | testIntrospectorBehavior |
| P1 | getter/setter 冲突：**JDK-9+ 以 getter 为准**（PropertyInfo 先处理 getter 注解即止；JDK-8 相反，setter 胜出——版本差异） | testGetterSetterConflict |
| P1 | 版本差异：`Class.forName("java.beans.BeanProperty"/"java.beans.JavaBean")` 在本 JDK 存在 | testVersionDifferences |

## 二、参考来源
- Oracle Java SE 27 javadoc `java/beans/Transient.html`；OpenJDK27 `com/sun/beans/introspect/PropertyInfo.java`（getter 优先实现）。

## 三、易错规范细节
- 断言按「值属性 transient=Boolean.TRUE/FALSE」写，**不要**断言属性消失。
- 冲突优先级为版本差异：JDK-8 setter 胜、JDK-9+ getter 胜；双侧测试文件此处断言相反。
- JDK-27 下无注解的属性描述符也可能带 `expert/hidden/...` 等键（BeanProperty 机制填充），断言「无 transient 属性」时用 `getValue("transient")==null`，不要断言 attributeNames 为空。
