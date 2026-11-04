# java.beans.beancontext.BeanContextServiceAvailableEvent — 设计案（JDK-8 侧）

## 一、API 功能
服务可用事件。extends BeanContextEvent（→ EventObject，source 为 transient）；恰 1 个公共构造器 (BeanContextServices, Class)；方法 getSourceAsBeanContextServices()/getServiceClass()/getCurrentServiceSelectors()；字段 protected Class serviceClass（非 transient）；serialVersionUID -5333985775656400778L。

## 二、使用方式
BeanContextServicesSupport.addService 成功时构造并 fire 给所有注册的 BeanContextServicesListener 与实现了 BeanContextServices 的 child 上下文；getCurrentServiceSelectors 委托**事件构造时的 source 上下文**在调用时刻查询（活视图）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServiceAvailableEvent
- JavaBeans 规范 1.01-A（服务事件）
- OpenJDK 8u 源码 `BeanContextServiceAvailableEvent.java` + `BeanContextServicesSupport.fireServiceAdded`；`~/git/jdk` JDK-27 主线一致（含 serialVersionUID）
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致（null source → IAE、序列化后 getCurrentServiceSelectors → NPE 等）

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥40，实际 61）：
1. testWhiteBox（原 6 断言保留 + 扩展）：具体类/extends BeanContextEvent/3 个 hasMethod（原样保留）；方法名集合恰 3；恰 1 公共构造器且参数型恰 (BeanContextServices, Class)；serviceClass protected 且非 transient；Serializable + serialVersionUID 精确值；继承 BeanContextEvent 的 4 个传播方法；非 deprecated（JDK-8）。
2. testBlackBox（原 4 断言保留 + 扩展）：注册服务后手工构造事件——source/serviceClass/getBeanContext/getCurrentServiceSelectors 非 null。
3. testSemantics（真实 addService 流捕获事件）：getSource/getSourceAsBeanContextServices/getBeanContext 三者身份相等且 == 上下文；getBeanContext 类型 BeanContext；serviceClass 精确；**getCurrentServiceSelectors 活委托**：注册期间逐个返回 provider 的 alpha/beta；revoke 后同一事件再查 → null（非快照！）；从未注册的类 → null；手工事件在已注册上下文上 → 非 null。
4. testNullMatrix：null source → IllegalArgumentException("null source")（EventObject 契约，不是 NPE）；null serviceClass 被接受存储（getServiceClass null、selectors 经 source 查 null 表、toString 不抛）。
5. testPropagation：默认未传播/propagatedFrom null；set→isPropagated true + 身份往返；set(null) 复位；toString 以 "类名[source=" 开头并含 source。
6. testSerialization：round-trip 后 serviceClass 存活、propagatedFrom 存活；**EventObject.source transient → getSource/getSourceAsBeanContextServices/getBeanContext 全 null**；此后 getCurrentServiceSelectors() → NPE（对 null source 解引用）；未传播事件往返后仍未传播；toString 仍可用。
易错规范细节：① getCurrentServiceSelectors 不是快照：查的是 source 上下文的**当前**注册表，服务撤销后同一事件返回 null；② null source 抛 IAE（EventObject 语义）而 null serviceClass 完全合法；③ 序列化后一切基于 source 的查询要么 null 要么 NPE，只有 serviceClass/propagatedFrom 存活。
