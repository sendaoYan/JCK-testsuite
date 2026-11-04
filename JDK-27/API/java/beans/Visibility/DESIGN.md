# java.beans.Visibility — 设计案

## 1. API 功能
GUI 可用性接口。needsGui/dontUseGui/okToUseGui/avoidingGui。

## 2. Java 用户如何使用
无 GUI 环境下 Bean 声明避免使用 GUI。

## 3. 开源代码实际用法调研
JDK 源码 `Visibility.java`；beancontext 实现。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现 needsGui/dontUseGui/okToUseGui/avoidingGui 状态切换。

## 5. 深化补充（batch beansG，59 断言）
旧 4 → 新 59 断言，与 JDK-8 侧文件同构：
- **testWhiteBoxStructure**：恰好 4 个声明方法且成员集精确（needsGui/dontUseGui/okToUseGui/avoidingGui）；逐方法签名与返回类型；public abstract、非 default、无异常/注解；无字段。
- **testGuiBeanMatrix**（嵌套 GuiBean）：needsGui 恒 true 不随 dontUseGui/okToUseGui 变化（内在需求 vs 避开状态正交）；avoidingGui 翻转、重复 dontUseGui 幂等、okToUseGui 复位。
- **testHeadlessBeanMatrix**（嵌套 HeadlessBean）：needsGui false 不变量；avoidingGui 仅在 dontUseGui 后为 true。
- **testInstanceIndependence**：avoiding 状态按实例隔离。
- **testBeanContextIntegration**（BeanContextSupport deprecated-for-removal，@SuppressWarnings 继续覆盖）：instanceof Visibility + isAssignableFrom；两 bean 嵌入 ctx 后各自可见性语义原样保留、membership 不受影响。
- **testCrossTypeAssignability**：与 DesignMode/Customizer 无关、非 Serializable/EventListener。
