# java.beans.beancontext.BeanContextServiceProviderBeanInfo — 设计案（JDK-8 侧）

## 一、API 功能
服务提供者 BeanInfo 接口。extends java.beans.BeanInfo（8 个抽象方法：getBeanDescriptor/getEventSetDescriptors/getDefaultEventIndex/getPropertyDescriptors/getDefaultPropertyIndex/getMethodDescriptors/getAdditionalBeanInfo/getIcon(int) + 4 个 ICON_* 常量），自身仅声明 getServicesBeanInfo()→BeanInfo[]。非 Serializable、非 EventListener，与 BeanContextServiceProvider 无类型层面关联。

## 二、使用方式
服务提供者另写一个实现本接口的 BeanInfo 类，getServicesBeanInfo 返回各服务的 BeanInfo 数组，供 GUI 构建器枚举服务元数据；仅是约定通道，beancontext 运行时不消费它。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServiceProviderBeanInfo 与 java.beans.BeanInfo
- JavaBeans 规范 1.01-A（BeanInfo 章节）
- OpenJDK 8u 源码 `BeanContextServiceProviderBeanInfo.java`、`SimpleBeanInfo.java`（默认实现全部 null / -1）；`~/git/jdk` JDK-27 主线一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥35，实际 58）：
1. testWhiteBox（原 4 断言保留 + 扩展）：接口/无超类/extends BeanInfo/hasMethod getServicesBeanInfo（原样保留）；方法名集合恰 [getServicesBeanInfo]；直接父接口恰 BeanInfo；与 BeanContextServiceProvider 互不 assignable；BeanInfo 恰 8 方法逐名断言；**ICON 常量值 16x32 顺序易错：ICON_COLOR_16x16=1、ICON_COLOR_32x32=2、ICON_MONO_16x16=3、ICON_MONO_32x32=4**；getServicesBeanInfo public abstract、返回 BeanInfo[]、无参无 checked 异常；记录型断言：provider 接口无 getServicesBeanInfo/getBeanDescriptor。
2. testBlackBox（原 3 断言保留 + 扩展）：SimpleBeanInfo 子类返回数组身份/长度/getBeanDescriptor null。
3. testArraySemantics：**返回数组按身份透传，无防御拷贝**（== 断言、重复调用同实例）；元素身份保留；外部改数组后续调用可见（活视图）；空数组合法；null 合法；元素可以是嵌套的自身接口实现；运行时类型恰 BeanInfo[]。
4. testInheritedDefaults：SimpleBeanInfo 默认——getBeanDescriptor/getPropertyDescriptors/getEventSetDescriptors/getMethodDescriptors/getAdditionalBeanInfo 全 null；getDefaultPropertyIndex/getDefaultEventIndex 均为 **-1**；getIcon 对任意 kind（含 0、MAX_VALUE）null 且不校验不抛；纯接口实现（9 方法全写）编译并行为一致；经 BeanInfo 父类型引用多态使用与条件向下转型。
易错规范细节：① 数组无拷贝：调用方可变数组内容即服务元数据内容；② null 与空数组都是合法返回（无强制非空）；③ SimpleBeanInfo 的 getIcon 对非法 kind 也静默 null；④ 接口本体与 provider 接口之间没有任何编译期联系——纯命名约定（getServicesBeanInfo 也不会被 BeanContextServicesSupport 调用）。
