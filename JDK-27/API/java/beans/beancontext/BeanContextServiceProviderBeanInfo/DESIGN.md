# java.beans.beancontext.BeanContextServiceProviderBeanInfo — 设计案

## 1. API 功能
服务提供者 BeanInfo 接口（extends BeanInfo）。getServicesBeanInfo() 返回服务 BeanInfo 数组。

## 2. Java 用户如何使用
服务提供者实现该接口暴露其服务的 BeanInfo。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServiceProviderBeanInfo.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + extends BeanInfo。黑盒（`testBlackBox`）：匿名实现 getServicesBeanInfo 与继承的 BeanInfo 方法。

## 5. 深化追加（批次 beansI，2026-11）
移除了错误的 `@key headful`。追加深度（8→62 断言）：单方法精确清单、返回 BeanInfo[]；BeanInfo 8 方法逐名 + **ICON 常量实测值：ICON_COLOR_16x16=1、ICON_COLOR_32x32=2、ICON_MONO_16x16=3、ICON_MONO_32x32=4**；数组语义——身份透传无防御拷贝、重复调用同实例、外部变异可见（活视图）、空数组/null 均合法、嵌套自指实现、运行时类型 BeanInfo[]；SimpleBeanInfo 继承默认——6 个 getter 全 null、两个 index 默认 **-1**、getIcon 对任意 kind（0/MAX_VALUE）null 不校验；纯接口实现（9 方法全写）+ BeanInfo 父类型多态使用；记录：provider 接口无任何 BeanInfo 方法（纯命名约定）；JDK-27 侧 @Deprecated(forRemoval=true,since=23) 断言。双侧行为实测一致。
