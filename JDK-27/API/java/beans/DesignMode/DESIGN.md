# java.beans.DesignMode — 设计案

## 1. API 功能
设计模式接口。常量 PROPERTYNAME="designTime"；setDesignTime/isDesignTime。

## 2. Java 用户如何使用
BeanContext 等实现该接口区分设计期/运行期。

## 3. 开源代码实际用法调研
JDK 源码 `DesignMode.java`；beancontext 实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + PROPERTYNAME。黑盒（`testBlackBox`）：匿名实现 set/isDesignTime。

## 5. 深化补充（batch beansG，45 断言）
旧 3 → 新 45 断言，与 JDK-8 侧文件同构：
- **testWhiteBoxStructure**：恰好 2 个声明方法（成员集精确）；签名（setDesignTime(boolean)→void、isDesignTime()→boolean）；public abstract、非 default、无异常/注解；PROPERTYNAME public static final String=="designTime" 且为唯一声明字段。
- **testImplementationRoundTrip**（嵌套 RecordingDesignMode）：初值 false、往返、幂等、last-set-wins、实例隔离。
- **testInterfaceIsStateless**：Beans.setDesignTime(true)（finally 恢复）不触及实现自有 flag——接口无状态。
- **testBeanContextIntegration**（BeanContextSupport 在 JDK-27 为 deprecated-for-removal，@SuppressWarnings 继续覆盖）：instanceof DesignMode、自身 flag 往返、与 Beans 全局开关**双向不传播**（探针两侧一致）。
- **testCrossTypeAssignability**：与 Customizer/Visibility 无关、非 Serializable/EventListener。
