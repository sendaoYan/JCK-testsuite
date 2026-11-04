# java.beans.BeanProperty — 设计案

## 1. API 功能
注解（@Target METHOD/FIELD）。bound/expert/hidden/preferred/required/visualUpdate/description/enumerationValues。bound 默认 true，其余 boolean 默认 false，description 默认 ""。

## 2. Java 用户如何使用
`@BeanProperty(bound=true)` 标注 getter 声明绑定属性。

## 3. 开源代码实际用法调研
JDK 源码 `BeanProperty.java`；BeanInfo 生成。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型。黑盒（`testBlackBox`）：反射验证默认值（bound=true 等）与显式覆盖。

---

# 深化补充（2026-11-05，batch beansC）

> 勘误：上文「Target METHOD/FIELD」不准确——实测 Target 恰为 `{METHOD}`；且 `bound` 默认 true、其余布尔默认 false、`description` 默认 ""、`enumerationValues` 默认空 String[]。

## 一、API 功能（补充）
- 8 元素：`bound`(默认 true)、`expert`/`hidden`/`preferred`/`required`/`visualUpdate`(默认 false)、`description`(默认 "")、`enumerationValues`(默认 `{}`)。
- Target({METHOD}) + RUNTIME + Documented；JDK-9 引入，JDK-8 无此类。

## 二、新增覆盖
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 8 元素默认值逐一反射断言（getDefaultValue）；元素类型逐一断言；恰 8 个声明方法 | testWhiteBox |
| P0 | Target({METHOD}) 精确、RUNTIME、Documented；注解代理 equals/hashCode/annotationType | testWhiteBox |
| P0 | 行为：expert/hidden/preferred/description 全开 → PropertyDescriptor 的 isExpert/isHidden/isPreferred/getShortDescription 精确生效；displayName 不变；`getValue("expert"/"hidden")==Boolean.TRUE` | testFlaggedDescriptor |
| P0 | **bound 矩阵**（实测）：bound=true 且 Bean 有 add/removePropertyChangeListener → isBound true；bound=false 同 Bean → false；无注解同 Bean → true；bound=true 但**无**监听方法 → false（注解单独不能凭空声明 bound） | testBoundMatrix |
| P0 | enumerationValues={"ONE","TWO"} 指向本类 public static final int 字段 → `getValue("enumerationValues")` 为 Object[6] 三元组 {名, 值(Integer), 原串} | testEnumerationValues |
| P1 | enumerationValues 引用非 static 字段 → 不解析 → 空 Object[0]（无异常） | testEnumerationValues |
| P1 | required/visualUpdate → `getValue("required"/"visualUpdate")==Boolean.TRUE` | testRequiredVisualUpdate |
| P1 | 布尔 is-getter 上注解同样生效（isExpert/getShortDescription/boolean 类型） | testBooleanIsGetter |

## 三、参考来源
- Oracle Java SE 27 javadoc `java/beans/BeanProperty.html`（enumerationValues 的字段引用语法说明）。
- OpenJDK27 `com/sun/beans/introspect/PropertyInfo.java`：`initialize(MethodInfo)`——bound=false 才写 map（`bound && info.is(bound)` 求与）；enumerationValues 解析 lastIndexOf('.') 分割 + `getField` + static/assignable 校验，任一条目失败则整体不写（空数组兜底在 PropertyDescriptor 构造器）。

## 四、易错规范细节
- **bound 语义**：`setBound(propertyChangeSource && annotation不否决)`——注解 bound=true 只是「不否决」，还必须 Bean 提供 add/removePropertyChangeListener；单凭注解断言 isBound()==true 是最常见的错误断言。
- **enumerationValues 字符串格式**：`"字段名"`（本类 public static 字段）或 `"完全限定类名.字段名"`（至少含一个 '.' 才按类解析）；`"ONE javax.swing.JOptionPane one"` 这类三段空格式是**无效的**（会触发 ClassFinder 异常打印后整体丢弃）。
- 解析失败的兜底是**空 Object[0]**（PropertyDescriptor 构造器 `if (values == null) values = new Object[0]`），因此 `getValue("enumerationValues")` 永不为 null。
- expert/hidden 同时写入 `setValue("expert"/"hidden", Boolean)` 兼容属性与 setExpert/setHidden 两处——断言两通道都应验证。
- description 只改 `getShortDescription()`，`getDisplayName()` 保持属性名。
