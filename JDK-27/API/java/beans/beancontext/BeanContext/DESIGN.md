# java.beans.beancontext.BeanContext — 设计案

## 1. API 功能
Bean 上下文接口（extends BeanContextChild, Collection, DesignMode, Visibility）。常量 globalHierarchyLock；instantiateChild/getResource/getResourceAsStream/addBeanContextMembershipListener/removeBeanContextMembershipListener。

## 2. Java 用户如何使用
Bean 容器实现该接口管理子 Bean：instantiateChild 动态加载子 Bean，getResource 按子 Bean 类加载器查资源，membership 监听子 Bean 增删。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContext.java`；beancontext 包内 BeanContextSupport 实现之。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + globalHierarchyLock + 四个父接口。黑盒（`testBlackBox`）：instantiateChild 实例化 String、membership 监听器 add/remove、getResourceAsStream/getResource 空参数 NPE。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 8 → 49。新增：直接父接口恰 4 个（排序集合精确相等）且非 assignable BeanContextServices/Serializable/EventListener；声明方法恰 5 个；instantiateChild 声明 IOException+ClassNotFoundException、资源方法声明 IllegalArgumentException；globalHierarchyLock 反射契约（public static final Object、稳定实例、可作 monitor）；经接口类型引用以 BeanContextSupport 驱动的 Collection/DesignMode/Visibility/BeanContextChild 四表面行为；成员监听 add/remove + 触发；资源负例矩阵（NPE/IAE）与成员命中。
- JDK-27 与 JDK-8 完全一致（该接口自 JDK-23 起 deprecated for removal，仅编译告警）。
