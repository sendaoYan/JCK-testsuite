# java.beans.Customizer — 设计案

## 1. API 功能
Bean 定制器接口。setObject/addPropertyChangeListener/removePropertyChangeListener。

## 2. Java 用户如何使用
实现 Customizer 提供属性面板；BeanDescriptor.getCustomizerClass 指定之。

## 3. 开源代码实际用法调研
JDK 源码 `Customizer.java`；IDE 定制器。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：匿名实现 setObject 分发。

## 5. 深化补充（batch beansG，56 断言）
旧 3 → 新 56 断言，与 JDK-8 侧文件同构：
- **testWhiteBoxStructure**：恰好 3 个声明方法且成员集精确（setObject(Object)/add/removePropertyChangeListener(PropertyChangeListener)）；void 返回、public abstract、非 default、无异常、无注解；接口无超接口/泛型/注解。
- **testSetObjectContract**（嵌套 public static BasicCustomizer）：setObject(null) 接受、同一性记录、last-set-wins、任意类型。
- **testPropertyChangeWiring**：自定义 PropertyChangeSupport 完整接线——source/name/old/new 逐字段、双监听器、remove 后静默、重复 remove no-op、移除未注册无害、**同一监听器双注册 → 每次 fire 两次通知、单次 remove 只注销一条**（ArrayList.remove 单条语义，两侧实测一致）、old==new 抑制。
- **testIntrospectorIntegration**：显式 PlainBeanBeanInfo 发布 BasicCustomizer.class → getBeanDescriptor().getCustomizerClass() 精确取回；单参 BeanDescriptor customizerClass 为 null。
- **testCrossTypeAssignability / testAnonymousImplementation**：非 EventListener/Serializable、与 DesignMode/Visibility/BeanContext 无关；`!Component.isAssignableFrom` 记录 headless 取舍；匿名实现 setObject 分发。
