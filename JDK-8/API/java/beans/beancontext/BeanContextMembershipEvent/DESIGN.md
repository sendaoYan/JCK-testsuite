# java.beans.beancontext.BeanContextMembershipEvent — 设计案（JDK-8 侧）

## 一、API 功能
成员变更事件：extends BeanContextEvent；两个公共构造器 (BeanContext, Collection) 与 (BeanContext, Object[])（后者 children = Arrays.asList(changes)——活视图）；size/contains(equals 语义)/toArray()/iterator()。children 为 null → NPE；source 为 null → IAE（EventObject 契约）。serialVersionUID 3499346510334590959L。

## 二、使用方式
BeanContextSupport 在 add/remove 成功时构造并 fire 给 BeanContextMembershipListener；事件携带恰好受影响的子项集合。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextMembershipEvent（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextMembershipEvent.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextMembershipEvent.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥40，实际 58）：
1. testWhiteBox：具体类、superclass BeanContextEvent、contains/iterator/size/toArray、恰 2 个构造器、serialVersionUID。
2. testBlackBox（原断言保留 + 扩展）：getBeanContext/getSource 身份；两种构造器 size/contains/toArray/iterator；迭代器保身份（==）；toArray 保身份。
3. testContentsSemantics：空子项（size 0/toArray 空/iterator 无 hasNext/contains false）；equals 语义 contains（new String("a") 命中；null 不命中）；toArray 每次返回新数组；多子项迭代恰覆盖一次；越界 next → NoSuchElementException；hasNext 幂等；Set 构造器；Object[] 构造器保留活视图（外部数组改动可见——实测语义，非快照）。
4. testNegativeMatrix：(bc, null 数组)→NPE；(bc, null Collection)→NPE；(null, 空)→IAE（两种构造器）；数组构造器 iterator().remove() → UnsupportedOperationException；ArrayList 构造器 remove 生效且 size 反映。
5. testPropagation：默认 null/未传播；set 生效；set(null) 复位；基类型引用 getBeanContext 正确。
6. testSerialization：source transient → getSource()/getBeanContext() 为 null；children 存活；propagatedFrom 存活为副本；未传播事件往返后仍未传播。
易错细节：① source transient 是 EventObject 级别的，getBeanContext() 往返后返回 null 而非抛异常；② Object[] 构造器是活视图（Arrays.asList）；③ null source 抛 IAE 不是 NPE（EventObject 用 "null source" 的 IAE）。
