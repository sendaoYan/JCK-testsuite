# java.beans.BeanInfo — 设计案（JDK-8）

> 覆盖目标：接口结构契约（常量精确值 + 8 方法完整清单 + 精确返回类型）+ Introspector 产出的真实 BeanInfo 行为契约（默认索引/图标/附加信息/方法描述符覆盖/内部数组共享）。headless 安全。

## 一、API 功能
- Bean 元信息只读接口，8 个方法：`getBeanDescriptor`、`getPropertyDescriptors`、`getEventSetDescriptors`、`getMethodDescriptors`、`getDefaultPropertyIndex`、`getDefaultEventIndex`、`getIcon(int)`、`getAdditionalBeanInfo`。
- 4 个图标常量（精确值）：`ICON_COLOR_16x16=1`、`ICON_COLOR_32x32=2`、`ICON_MONO_16x16=3`、`ICON_MONO_32x32=4`。

## 二、使用方式
- 工具类实现该接口（或继承 `SimpleBeanInfo`）描述自定义 Bean；`Introspector.getBeanInfo(Class)` 返回自动生成的实现。
- GUI 构建器读取默认属性索引/默认事件索引/图标做属性面板。

## 三、参考来源
- Oracle Java SE 8 javadoc `java/beans/BeanInfo.html`（常量值与 8 方法签名）。
- JavaBeans 规范 1.01-A 第 7/8 章（BeanInfo 与事件集）。
- OpenJDK8u `test/jdk/java/beans/BeanInfo`（回归思路：常量与方法清单反射断言）。

## 四、测试用例设计 + 易错规范细节
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 图标常量精确 int 值 1/2/3/4 | testIconConstants |
| P0 | 接口 8 方法完整清单（getDeclaredMethods 逐一断言，且公开方法恰为这 8 个） | testInterfaceInventory |
| P0 | 8 方法精确返回类型（含 `getIcon -> java.awt.Image`、数组类型三兄弟） | testInterfaceInventory |
| P0 | Introspector 产出的 BeanInfo：默认属性/事件索引 -1、getIcon（4 常量 + 越界 0/99/-1）全 null | testRealBeanInfoBehavior |
| P0 | 方法描述符逐公开方法 find-by-name 覆盖（含 Object 方法），数量 == `getMethods().length` | testRealBeanInfoBehavior |
| P1 | `getMethodDescriptors()[i].getMethod()` 往返（名称/声明类/参数个数） | testRealBeanInfoBehavior |
| P1 | `getAdditionalBeanInfo()` null；`getEventSetDescriptors()` 空数组而非 null（与 SimpleBeanInfo 的 null 形成对照） | testRealBeanInfoBehavior |
| P2 | 内部数组非防御拷贝：重复调用返回同一引用、原地改动可见（实测记录，测试内恢复 + flushCaches） | testRealBeanInfoBehavior |

### 易错规范细节
- **Introspector 产出的 `getEventSetDescriptors()` 对无事件 Bean 返回长度 0 的数组**，而 `SimpleBeanInfo.getEventSetDescriptors()` 返回 null —— 两者语义不同，断言时必须区分。
- MethodDescriptor 覆盖**全部公开方法**（含继承自 Object 的 getClass/wait/notify/notifyAll/hashCode/equals/toString），且方法重载 Bean 无桥接方法时数量恰等于 `Class.getMethods().length`。
- 属性/方法描述符数组是**内部存储的直接引用**（无防御拷贝），消费方不得原地修改；本测试改动后立即恢复并 `Introspector.flushCaches()`。
- `getIcon` 对越界 code（0/99/负数）不抛异常、只返回 null（javadoc 未约束，实测记录）。
