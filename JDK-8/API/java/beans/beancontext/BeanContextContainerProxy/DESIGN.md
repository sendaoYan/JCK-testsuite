# java.beans.beancontext.BeanContextContainerProxy — 设计案（JDK-8 侧）

## 一、API 功能
容器代理接口。仅声明 getContainer()→java.awt.Container（精确类型，无父接口）。非 Serializable、非 EventListener；与 BeanContextChild/BeanContextChildComponentProxy/BeanContextProxy 相互独立。

## 二、使用方式
有关联 AWT 容器的上下文/Bean 实现本接口向 GUI 构建器暴露容器供子组件挂载；beancontext 运行时**不消费**本接口——纯约定通道。容器永不显示（测试保持 headless）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextContainerProxy
- JavaBeans 规范 1.01-A（容器代理章节）
- OpenJDK 8u 源码：beancontext 包内无任何调用点（grep 实证）；java.awt.Panel headless 可构造性双侧实测
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥30，实际 33）：
1. testWhiteBox（原 3 断言保留 + 扩展）：接口/无超类/hasMethod getContainer（原样保留）；方法名集合恰 1；无父接口；public abstract、返回型恰 Container、无参无 checked 异常；非 EventListener/非 Serializable；非 BeanContextChild 子类型；与 BeanContextChildComponentProxy/BeanContextProxy 独立；Container IS-A Component（格点记录）；非 @FunctionalInterface。
2. testBlackBox（原 Panel 断言保留 + 扩展）。
3. testSemantics：真实实现（java.awt.Panel——headless 下可构造，双侧实证）身份稳定、具体类型保留、经 Container 与 Component 两种父类型使用；null 实现合法；两 Bean 容器独立；三实现多态分派计数（2 容器 + 1 null）；普通 AWT 嵌套（add/remove 内部 Panel + getComponentCount）与代理契约无关（记录型断言）；实现抛异常原样传播不包装。
4. testReflectionDispatch：经接口 Method.invoke 分派到具体/null 实现；声明类是接口本身。
易错规范细节：① **java.awt.Panel 在 -Djava.awt.headless=true 下可构造，Label 不行**——headless 用例选 Panel；② null 返回合法；③ 接口无人消费——断言集中在结构精确性与实现方契约（身份稳定、异常透传、多态分派）。
