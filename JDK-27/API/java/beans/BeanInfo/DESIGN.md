# java.beans.BeanInfo — 设计案

## 1. API 功能
Bean 信息接口。常量 ICON_*、USE_ALL_BEANINFO；getBeanDescriptor/getPropertyDescriptors/getEventSetDescriptors/getMethodDescriptors/getDefaultPropertyIndex/getDefaultEventIndex/getIcon/getAdditionalBeanInfo。

## 2. Java 用户如何使用
自定义 BeanInfo 描述 Bean 属性/事件/方法/图标；Introspector 返回。

## 3. 开源代码实际用法调研
JDK 源码 `BeanInfo.java`；JavaBeans 规范核心接口。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 常量 + 关键方法。黑盒（`testBlackBox`）：SimpleBeanInfo 默认值。

---

# 深化补充（2026-11-05，batch beansC）

## 一、新增结构契约
- 图标常量精确值逐一断言：`ICON_COLOR_16x16=1`、`ICON_COLOR_32x32=2`、`ICON_MONO_16x16=3`、`ICON_MONO_32x32=4`。
- 8 方法完整清单（getDeclaredMethods 逐一断言 + 公开方法恰为这 8 个）：getAdditionalBeanInfo/getBeanDescriptor/getDefaultEventIndex/getDefaultPropertyIndex/getEventSetDescriptors/getIcon/getMethodDescriptors/getPropertyDescriptors。
- 8 方法精确返回类型（BeanInfo[]/BeanDescriptor/int/int/EventSetDescriptor[]/java.awt.Image/MethodDescriptor[]/PropertyDescriptor[]）。

## 二、新增行为契约（Introspector 产出的真实 BeanInfo，testRealBeanInfoBehavior）
- 默认索引：`getDefaultPropertyIndex()==-1`、`getDefaultEventIndex()==-1`（无 @JavaBean 注解时；JDK-27 侧与 BeanProperty/JavaBean 注解的交互由各自测试文件覆盖）。
- `getIcon` 全 null：4 常量 + 越界 0/99/-1。
- `getAdditionalBeanInfo()` null；`getEventSetDescriptors()` 是**空数组而非 null**（与 SimpleBeanInfo 的 null 对照）。
- 方法描述符逐公开方法 find-by-name 覆盖（含 Object 方法），数量 == `Class.getMethods().length`；`MethodDescriptor.getMethod()` 名称/声明类/参数个数往返。
- 内部数组非防御拷贝：重复调用同引用、原地改动可见（实测记录，测试内恢复 + flushCaches）。

## 三、参考来源
- Oracle Java SE 27 javadoc `java/beans/BeanInfo.html`（java.desktop 模块）。
- OpenJDK27 `test/jdk/java/beans/BeanInfo` 回归思路；`java/beans/GenericBeanInfo.java`（数组直返实现）。

## 四、易错规范细节
- JDK-9+ Introspector 产出的 PropertyDescriptor 恒带 `expert/hidden/required/visualUpdate/enumerationValues` 值属性（BeanProperty 默认填充），JDK-8 无——跨版本断言属性键集合时必须区分（本测试只用 SampleBean 不断言键集合）。
- 其余与 JDK-8 侧一致：EventSetDescriptors 空数组语义、方法描述符全量覆盖、共享数组不可改。
