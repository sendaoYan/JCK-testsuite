# java.beans.JavaBean — 设计案

## 1. API 功能
注解（@Target TYPE）。description/defaultProperty/defaultEventSet（默认均 ""）。

## 2. Java 用户如何使用
`@JavaBean(description="...", defaultProperty="x")` 声明 Bean 元信息。

## 3. 开源代码实际用法调研
JDK 源码 `JavaBean.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射读取三个属性。

---

# 深化补充（2026-11-05，batch beansC）

## 一、API 功能（补充）
- Target({TYPE}) + RUNTIME + Documented + **非 Inherited**；3 元素全 String、默认全 ""。
- JDK-9 引入；JDK-8 无此类（TransientTest 的版本差异段有 Class.forName 负断言）。

## 二、新增覆盖
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 3 元素默认 ""（getDefaultValue）；类型 String；恰 3 个声明方法；非 @Inherited | testWhiteBox |
| P0 | Target({TYPE}) 精确、RUNTIME、Documented；代理 equals/hashCode/annotationType | testWhiteBox |
| P0 | 行为：defaultProperty="name" → `getDefaultPropertyIndex()` 落在名为 name 的描述符上（find-by-name，不断言数组顺序）；defaultEventSet="foo" 同理落在 foo 事件集；索引均在数组界内 | testDefaultIndexes |
| P0 | 行为：description="my bean" → BeanDescriptor `getShortDescription()`；displayName 不动；beanClass/customizerClass/二进制名 | testBeanDescriptorDescription |
| P1 | 负例：defaultProperty 指向不存在的属性 / defaultEventSet 指向不存在的事件集 → 索引保持 -1（实测）；裸 @JavaBean 等价于全默认（-1/-1 且 shortDesc==displayName） | testMissingNames |
| P1 | defaultProperty 可指向**继承来的**属性（子类标注、父类属性命中） | testInheritedPropertyAsDefault |
| P1 | 对照组：未标注同类 → -1/-1 | testControl |

## 三、参考来源
- Oracle Java SE 27 javadoc `java/beans/JavaBean.html`。
- OpenJDK27 `java/beans/Introspector.getTargetPropertyInfo`（`defaultPropertyName` 注入与结果数组扫描实现）与 `getTargetEventInfo`。

## 四、易错规范细节
- 默认索引在**结果数组内按名字扫描**命中（`defaultPropertyIndex = i`），属性数组顺序（含 Object 的 class 属性插入位置）不保证——断言必须 find-by-name。
- 名字无命中时索引保持构造默认 -1，**不抛异常**。
- 注解只对标注类自身生效（非 @Inherited），子类不继承；但 defaultProperty 指向的**属性**可以来自父类。
- description 只影响 `BeanDescriptor.getShortDescription()`；`getDisplayName()` 默认仍是类的二进制简名。
