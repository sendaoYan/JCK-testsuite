# java.beans.beancontext.BeanContextChildComponentProxy — 设计案（JDK-8 侧）

## 一、API 功能
组件代理接口。仅声明 getComponent()→java.awt.Component（精确类型，无父接口）。非 Serializable、非 EventListener；与 BeanContextChild/BeanContextContainerProxy 相互独立。

## 二、使用方式
有可视组件的子 Bean 实现本接口向 GUI 构建器暴露组件；beancontext 运行时（BeanContextSupport 等）**不消费**本接口——纯约定通道。组件永不显示（测试保持 headless）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextChildComponentProxy
- JavaBeans 规范 1.01-A（组件代理章节）
- OpenJDK 8u 源码：beancontext 包内无任何调用点（grep 实证）；AWT headless 行为双侧实测
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 32）：
1. testWhiteBox（原 3 断言保留 + 扩展）：接口/无超类/hasMethod getComponent（原样保留）；方法名集合恰 1；无父接口；public abstract、返回型恰 Component、无参无 checked 异常；非 EventListener/非 Serializable；非 BeanContextChild 子类型；与 BeanContextContainerProxy 独立；**Container IS-A Component**（协变格点记录）；非 @FunctionalInterface 注解。
2. testBlackBox（原 JLabel 断言保留 + 扩展）。
3. testSemantics：真实实现（JLabel——**headless 下可构造**，双侧实证）身份稳定（每次同实例）、具体类型保留、经 Component 父类型使用；null 实现合法（无组件的 Bean）；两 Bean 组件独立；三实现多态分派计数（2 组件 + 1 null）；**java.awt.Label 在 headless 下构造抛 HeadlessException，java.awt.Panel 则可构造**（本测试用 Panel 做纯 AWT 例）；实现抛异常原样传播不包装。
4. testReflectionDispatch：经接口 Method.invoke 分派到具体/ null 实现；声明类是接口本身。
易错规范细节：① headless 可构造性分层：JLabel/Panel 可以、Label/Frame/Dialog 抛 HeadlessException——选样例组件时必须区分；② null 返回合法且常见（无 GUI 的 Bean）；③ 接口无人消费——断言集中在结构精确性与实现方契约（身份稳定、异常透传）。
