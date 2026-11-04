# java.beans.SimpleBeanInfo — 设计案（JDK-8）

> 覆盖目标：全部访问器默认值矩阵（null / -1）、getIcon 越界码、loadImage 三负例（实测均返回 null 不抛异常）、结构契约（公有无参构造器、非抽象、单一构造器）、子类覆盖语义。headless 安全（loadImage 对不存在的资源不触碰 AWT 工具包）。

## 一、API 功能
- `java.beans.BeanInfo` 的便利空实现：`getBeanDescriptor/getPropertyDescriptors/getEventSetDescriptors/getMethodDescriptors/getAdditionalBeanInfo` 默认 null，`getDefaultPropertyIndex/getDefaultEventIndex` 默认 -1，`getIcon(int)` 恒 null。
- `Image loadImage(String resourceName)`：按类路径资源加载图标，失败返回 null。

## 二、使用方式
- 自定义 BeanInfo 时继承本类，只覆盖需要的访问器；未覆盖项自动落到 null/-1 默认。

## 三、参考来源
- Oracle Java SE 8 javadoc `java/beans/SimpleBeanInfo.html`。
- JavaBeans 规范 1.01-A 第 8.5 节（BeanInfo 实现）。
- OpenJDK8u `test/jdk/java/beans/SimpleBeanInfo`（loadImage 失败路径回归思路）。

## 四、测试用例设计 + 易错规范细节
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 5 个 null/-1 默认访问器（原有用例保留） | testDefaults |
| P0 | getIcon 矩阵：4 常量 + 0/99/-5/Integer.MIN_VALUE 全 null，不抛异常 | testIconMatrix |
| P0 | loadImage(null)/("no-such.png")/("") 实测全部返回 null、不抛异常（内部 catch-all，实测记录） | testLoadImage |
| P0 | 结构：公有唯一无参构造器、非抽象、implements BeanInfo | testStructure |
| P1 | 子类覆盖 getPropertyDescriptors/getBeanDescriptor/getDefaultPropertyIndex 精确返回覆盖值 | testSubclass |
| P1 | 子类未覆盖项保持继承默认（getMethodDescriptors null、getIcon null、additional null） | testSubclass |
| P1 | 空数组覆盖（eventSets/additionalBeanInfo 返回 len=0 数组而非 null）与其余访问器仍 null 的组合 | testSubclass |
| P2 | 两个独立实例行为一致；经 BeanInfo 引用访问同一契约 | testSubclass |

### 易错规范细节
- **`loadImage(null)` 不抛 NPE**：内部 `getClass().getResource(null)` 抛出的异常被 catch(Exception) 吞掉后返回 null（实测 JDK-8 与 JDK-27 一致）。
- SimpleBeanInfo 的 `getEventSetDescriptors()` 返回 **null**，而 Introspector 生成实例返回**空数组**——跨实现断言时勿混淆。
- 覆盖 `getPropertyDescriptors` 时可 `new PropertyDescriptor("name", SomeClass.class, null, null)` 构造无读写方法的描述符，null 方法名合法。
- 本类被 Introspector 显式 BeanInfo 机制查找时要求命名规范（`<Bean>BeanInfo`），直接 new 实例的返回值即覆盖值，与 Introspector 无关。
