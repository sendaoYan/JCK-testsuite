# java.beans.beancontext.BeanContext — 设计案（JDK-8 侧）

## 一、API 功能
接口：extends BeanContextChild, java.util.Collection, java.beans.DesignMode, java.beans.Visibility（恰 4 个直接父接口，不含 BeanContextServices）。
声明方法恰 5 个：instantiateChild(String)（throws IOException, ClassNotFoundException）、getResourceAsStream(String, BeanContextChild)（throws IllegalArgumentException）、getResource(String, BeanContextChild)（throws IllegalArgumentException）、addBeanContextMembershipListener、removeBeanContextMembershipListener。
常量字段 globalHierarchyLock：public static final Object，跨上下文层级变更的全局锁。

## 二、使用方式
Bean 嵌套层级的核心抽象：Collection 表面管理子 Bean；DesignMode/Visibility 表面承载设计期与 GUI 状态；BeanContextChild 表面使上下文自身可嵌套；成员监听器感知层级变化；instantiateChild 在上下文内实例化 Bean；getResource* 以子 Bean 类加载器解析资源。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContext（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContext.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContext.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥35，实际 49）：
1. testStructure：4 个直接父接口排序后精确相等；逐一 assignable；非 assignable：BeanContextServices/Serializable/EventListener；声明方法恰 5 个（排序集合相等）；instantiateChild 声明 IOException+ClassNotFoundException；两个资源方法声明 IllegalArgumentException。
2. testGlobalLock：非 null；反射 public static final、类型 Object；两次访问同一实例；可作 monitor。
3. testBehaviorThroughInterface（以 BeanContextSupport 为实现、接口类型引用驱动）：Collection 表面（isEmpty/size/add/contains/for-each/toArray/containsAll/remove）；DesignMode 表面（默认 runtime、set/get 往返）；Visibility 表面（needsGui/avoidingGui 默认、okToUseGui/dontUseGui）；BeanContextChild 表面（嵌套上下文 inner.getBeanContext()==outer、detach 生效）；成员监听 add/remove 后不触发、add 触发 childrenAdded；instantiateChild 返回 String 且入册。
4. testResources：getResourceAsStream(null,null)/(name,null)/getResource(name,null)→NPE；getResource(null,member)→NPE；非成员 → IAE（两方法）；成员命中 JDK 资源。
易错细节：① BeanContext 不 extends BeanContextServices；② globalHierarchyLock 是 Object 类型常量；③ 资源方法 throws IAE 是"声明的"，null 入参实际抛 NPE（NPE 优先于成员校验）。
