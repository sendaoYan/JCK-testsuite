# java.beans.beancontext.BeanContextSupport.BCSChild — 设计案（JDK-8 侧）

## 一、API 功能
BeanContextSupport 的 protected 非 static 内部类，implements Serializable；每个子项一个 BCSChild 包装（HashMap<child, BCSChild>），BeanContextProxy 子项额外建 proxyPeer 包装（两把钥匙同加同删）。唯一包私有构造器 (外部引用, child, proxyPeer)；包私有访问器 getChild/setRemovePending/isRemovePending/isProxyPeer/getProxyPeer；serialVersionUID -5815286101609939109L。

## 二、使用方式
纯内部实现细节，无公开 API；只能通过 BeanContextSupport 公开表面（add/remove/size/contains/序列化）或反射观察其行为。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextSupport.BCSChild（https://docs.oracle.com/javase/8/docs/api/java/beans/beancontext/BeanContextSupport.BCSChild.html）
- JavaBeans 规范 1.01-A（事件模型与 BeanContext 章节）
- OpenJDK 8u 源码 `jdk/src/share/classes/java/beans/beancontext/BeanContextSupport.java` 与 `~/git/jdk`（JDK-27 主线同名文件）逐一比对；回归思路借鉴 `~/git/jdk/test/jdk/java/beans/beancontext/`（成员契约/事件传播/veto 路径等价类），代码自写。
- 负例异常类型以 JDK-8（jdk8u372-b07）与 JDK-27（27.0.1）双侧探针实测为准（两侧结果一致）。

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥20，实际 40）：
1. testWhiteBox：protected、非 public、非 static（内部类持 this$0）、非 final、extends Object、implements Serializable、declaring class 是 BeanContextSupport；唯一构造器包私有 3 参（BCS, Object, Object）；declared methods 恰为 5 个包私有访问器（排序后集合相等）；字段含 child/proxyPeer/removePending；serialVersionUID 稳定。
2. testBlackBox：add 创建包装（size/contains/iterator 可见）；重复 add 不建第二把钥匙；remove 后包装消失。
3. testProxyPeerWrappers：BeanContextProxy 子项一对包装（size==2、childrenAdded/Removed 各含两元素、proxy 子 BeanContext 被接线）；getBeanContextProxy() 返回 null → NullPointerException 且不入册。
4. testSerialization：包装随 BCS 序列化往返存活（含 proxy 对——反序列化后是副本，按类型断言 1 个 ProxyChild + 1 个 BeanContextChildSupport）。
易错细节：① BCSChild 是非 static 内部类（javap 见 this$0 字段），构造器首参即外部实例；② 反序列化后的子项是副本，equals 不成立（Object 身份比较），必须按类型/内容断言；③ proxy 对的成员事件 children 数组含两个元素。
