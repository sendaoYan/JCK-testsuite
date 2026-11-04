# java.beans.beancontext.BeanContextServiceRevokedEvent — 设计案（JDK-8 侧）

## 一、API 功能
服务撤销事件。extends BeanContextEvent；恰 1 个公共构造器 (BeanContextServices, Class, boolean)；方法 getSourceAsBeanContextServices()/getServiceClass()/isServiceClass(Class)/isCurrentServiceInvalidNow()；字段 protected Class serviceClass + **private boolean invalidateRefs**（均非 transient）；serialVersionUID -1295543154724961754L。

## 二、使用方式
BeanContextServicesSupport.revokeService(class, provider, revokeNow) 时对每个在册 requestor 与每个注册的 services listener 构造本事件（invalidate == revokeNow）；持有在用服务的监听器据此决定是否立刻停止使用（isCurrentServiceInvalidNow）。

## 三、参考来源
- Oracle javadoc，Java SE 8, java.beans.beancontext.BeanContextServiceRevokedEvent
- JavaBeans 规范 1.01-A（服务撤销语义）
- OpenJDK 8u 源码 `BeanContextServiceRevokedEvent.java` + `BeanContextServicesSupport.revokeService`/`BCSSChild.revokeService`（**每个在册 requestor 条目各收一个事件**）；`~/git/jdk` JDK-27 主线一致
- 双侧（jdk8u372-b07 / 27.0.1）探针实测一致

## 四、测试用例设计 + 易错规范细节
测试方法与断言要点（目标 ≥40，实际 81）：
1. testWhiteBox（原 6 断言保留 + 扩展）：具体类/extends BeanContextEvent/3 个 hasMethod（原样保留）；方法名集合恰 4；恰 1 公共构造器且参数型恰 (BeanContextServices, Class, boolean)；serviceClass protected 非 transient；invalidateRefs private（不属 API 表面）boolean 非 transient；4 个返回型；Serializable + serialVersionUID 精确值；继承表面。
2. testBlackBox（原 7 断言保留 + 扩展）：原有 source/serviceClass/isServiceClass 真假/invalidNow 真假断言原样保留。
3. testSemantics：三个 source 访问器身份相等；**isCurrentServiceInvalidNow 恰等于构造器旗标**（true/false 两例）；isServiceClass 矩阵——同类 true、异类 false、原始类 false、**null 参数 false（equals 契约，不抛）**、超类 false（精确匹配）；旗标不影响匹配；传播三态；toString 前缀。
4. testRealFlow（真实 revoke 流捕获）：revoke(false) → per-requestor 监听器恰 1 次、serviceClass 精确、invalidate=false、source==上下文、isServiceClass 命中；注册 listener 同旗标；re-add + 再请求 + revoke(true) → **上一轮 revoke(false) 遗留的在册 requestor 再次收到**（总 3 次：每个在册条目一个事件）； getService 事后 → null（本 API 无 ServiceUnavailableException）；hasService false。
5. testNullMatrix：null source → IAE；null serviceClass 存储但 isServiceClass(任意) → **NPE**（serviceClass.equals 解引用 null）；旗标/toString 不受影响。
6. testSerialization：true/false 旗标均存活；serviceClass 存活；isServiceClass 往返后仍工作；source transient → 三访问器 null；propagatedFrom 存活；toString 可用。
易错规范细节：① 事件数 = 在册 requestor 条目数（revoke(false) 不清条目，下一轮 revoke 会再次通知）；② isServiceClass(null) → false 而 serviceClass 为 null 时 isServiceClass(任意) → NPE，两个 null 语义不同；③ invalidateRefs 是 private 字段，只能经 isCurrentServiceInvalidNow 观察，但参与序列化。
