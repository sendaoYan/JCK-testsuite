# java.beans.beancontext.BeanContextChild — 设计案（JDK-8 侧）

## 一、API 功能
接口，无父接口。声明恰 6 个方法：setBeanContext(BeanContext)（throws PropertyVetoException）、getBeanContext()、addPropertyChangeListener(String, PropertyChangeListener)、removePropertyChangeListener(String, PropertyChangeListener)、addVetoableChangeListener(String, VetoableChangeListener)、removeVetoableChangeListener(String, VetoableChangeListener)。

## 二、使用方式
JavaBean 嵌入 BeanContext 的协议：beanContext 是 bound+vetoed 属性（命名固定 "beanContext"）；子 Bean 通过命名属性/否决监听器感知属性变化与即将发生的变化。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextChild（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextChild.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextChild.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥35，实际 40）：
1. testStructure：接口、无 superclass、0 个直接父接口；6 个方法名排序后精确相等；setBeanContext 声明 throws PropertyVetoException；4 个监听器方法参数签名 (String, PropertyChangeListener/VetoableChangeListener)；BeanContextChildSupport 实现之、BeanContext extends 之、BeanContextSupport 实现之。
2. testPropertySemantics（经 BeanContextChildSupport 驱动）：默认 null；set/get；null→bc 触发一次事件且字段精确（name/old null/new bc/source==child）；同值不发；bc→null 事件 old=bc new=null；命名监听器选择性好；veto 监听器抛 PropertyVetoException → setBeanContext 透传且属性不变；addPropertyChangeListener(null,l)/(name,null) 与 remove 不存在者均静默无操作；重复注册同名监听器触发两次、单次 remove 减半；VCL 注册/移除生效。
3. testDelegationPattern：自写 DelegatingChild 包装 BCCS 转发全部 6 个方法——set/get、监听器注册、detach 全部经委托生效；接口类型引用可互换。
易错细节：① 事件属性名固定 "beanContext"；② null name 注册经 BCCS→PCS 的命名分支是 no-op（实测不投递）；③ veto 后 rejectedSetBCOnce 语义见 BeanContextChildSupport 案。
