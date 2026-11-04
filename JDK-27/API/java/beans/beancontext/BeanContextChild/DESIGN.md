# java.beans.beancontext.BeanContextChild — 设计案

## 1. API 功能
Bean 上下文子接口。setBeanContext/getBeanContext、按名 add/removePropertyChangeListener、按名 add/removeVetoableChangeListener。

## 2. Java 用户如何使用
子 Bean 实现该接口以挂载到 BeanContext；setBeanContext 受 vetoable 约束。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextChild.java`；BeanContextChildSupport 实现之。

## 4. 测试用例设计 + 自动化实现
白盒：接口。黑盒（`testBlackBox`）：BeanContextChildSupport 的 setBeanContext/getBeanContext 往返。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试）。
- 断言数 4 → 40。新增：0 个直接父接口、声明方法恰 6 个（排序相等）、setBeanContext 声明 throws PropertyVetoException、监听器方法参数签名；经 BeanContextChildSupport 驱动的属性语义（默认 null、attach/detach 事件字段逐项、同值静默、命名选择性、veto → PVE 且属性不变、null-name/null-listener 静默 no-op、重复注册双发/单 remove 减半、VCL 注册移除）；自写 DelegatingChild 委托模式全表面转发。
- JDK-27 与 JDK-8 完全一致。
