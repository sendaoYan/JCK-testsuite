# java.beans.beancontext.BeanContextSupport.BCSIterator — 设计案（JDK-8 侧）

## 一、API 功能
BeanContextSupport 的 protected static final 嵌套类，implements Iterator<Object>；包私有构造器包一层 java.util.Iterator（HashMap keySet 迭代器）。hasNext/next 委托源迭代器；remove() 是无条件 no-op（成员变更只能走 add/remove 以保证事件语义）。

## 二、使用方式
纯内部实现细节；通过 BeanContextSupport.iterator() 获得。迭代器 fail-fast（结构性 add/remove 后 next 抛 ConcurrentModificationException）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextSupport.BCSIterator（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextSupport.BCSIterator.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextSupport.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥20，实际 32）：
1. testWhiteBox：protected static final、implements Iterator、declaring class、唯一包私有构造器 1 参 java.util.Iterator、声明 hasNext/next/remove。
2. testBlackBox：iterator() 实际类型为 BCSIterator；remove() no-op。
3. testIterationContract：空上下文 hasNext false、next → NoSuchElementException；内容与 toArray 集合相等（不断言顺序）；迭代不修改上下文；hasNext 幂等不前进；两次调用返回独立实例；next 前调 remove() 不抛 IllegalStateException（偏离 Iterator 契约，记录之）；重复 remove 安全；迭代到尽头后再 next → NoSuchElementException。
4. testFailFast：迭代中结构性 add → next 抛 CME；迭代中 remove → next 抛 CME；上下文本身不受影响。
易错细节：① remove() 是 no-op —— 与 Iterator 契约的 IllegalStateException 语义相反，是 BeanContextSupport 的显式设计；② fail-fast 源自 HashMap keySet 迭代器。
