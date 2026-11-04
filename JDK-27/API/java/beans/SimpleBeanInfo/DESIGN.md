# java.beans.SimpleBeanInfo — 设计案

## 1. API 功能
BeanInfo 便利空实现（implements BeanInfo），所有 getter 返回 null 或 -1。

## 2. Java 用户如何使用
继承 SimpleBeanInfo 只覆盖需要的 getter。

## 3. 开源代码实际用法调研
JDK 源码 `SimpleBeanInfo.java`；用户自定义 BeanInfo 常继承之。

## 4. 测试用例设计 + 自动化实现
白盒：implements BeanInfo。黑盒（`testBlackBox`）：各 getter 默认 null/-1 断言。

---

# 深化补充（2026-11-05，batch beansC）

## 一、新增覆盖
| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | getIcon 越界码矩阵：0/99/-5/Integer.MIN_VALUE 全 null 不抛异常 | testIconMatrix |
| P0 | loadImage 三负例实测：null / "no-such.png" / "" 均返回 null、不抛异常（内部 catch-all 吞掉 getResource 的 NPE，实测记录） | testLoadImage |
| P0 | 结构：公有唯一无参构造器、非抽象 | testStructure |
| P1 | 子类覆盖语义：getPropertyDescriptors/getBeanDescriptor/getDefaultPropertyIndex 覆盖值精确返回；未覆盖项保持 null 默认；空数组覆盖与其余 null 组合 | testSubclass |
| P2 | 两个独立实例一致；经 BeanInfo 引用访问同一契约 | testSubclass |

## 二、参考来源
- Oracle Java SE 27 javadoc `java/beans/SimpleBeanInfo.html`；OpenJDK27 `SimpleBeanInfo.java`（loadImage catch-all 实现）。

## 三、易错规范细节
- `loadImage(null)` 在 JDK-27 同样返回 null 而非 NPE（异常被 `catch (Exception)` 吞掉）。
- SimpleBeanInfo 的 `getEventSetDescriptors()` 返回 null；Introspector 生成实现返回空数组——语义不同。
- JDK-27 下自定义 BeanInfo 子类若配合显式 BeanInfo 搜索路径使用需 `--add-opens`/命名包；本测试直接实例化子类断言返回值，不触碰搜索路径。
