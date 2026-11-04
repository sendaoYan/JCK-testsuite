# javax.management.openmbean.CompositeDataInvocationHandler — 测试设计

## 1. API 功能
把 CompositeData 暴露成接口 getter 的 InvocationHandler：`new CompositeDataInvocationHandler(CompositeData)`
并配合 `Proxy.newProxyInstance` 生成代理。invoke 将无参且以 get/is 开头的方法映射到 CompositeData 的 item 值；
getCompositeData() 返回底层实例；equals/hashCode/toString 转发到底层 CompositeData。

## 2. Java 用户如何使用
定义接口（`String getName(); boolean isValid();`）→ new CompositeDataInvocationHandler(data) →
Proxy.newProxyInstance 得到强类型视图 → 调 getName() 等价于 data.get("Name")；getCompositeData() 拿回底层。
链路：CompositeData → handler → Proxy → getter 映射 → getCompositeData/equals/hashCode/toString。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射与 JMX 客户端工具将 open data 转为强类型视图时使用该 handler
  （`~/git/jdk25u-dev/src/java.management/share/classes/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/CompositeDataInvocationHandlerTest.java`
  覆盖 getter 映射与 Object 方法转发。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeDataInvocationHandlerTest.java）
- 白盒：public、implements InvocationHandler、构造器签名与 getCompositeData 方法存在。
- 黑盒：Proxy getter 映射 item 值（String 与 boolean isX getter）；getCompositeData 返回底层；
  equals/hashCode/toString 经代理转发与底层一致；非 getter 方法（带参/Object 之外）抛异常路径；
  构造 null CompositeData 抛 IAE/NPE 精确 message。


---

## 深化（2026-11，批次 F：openmbean 类型系统）

本轮追加维度与两侧实测结论（详细负例精确异常表见上文第 4 节，本轮已全部锁定为精确消息断言）：
- 序列化 round-trip 全覆盖（SimpleType 单例 readResolve ==、ArrayType/CompositeType/TabularType/
  CompositeDataSupport/TabularDataSupport equals 复活、null 字段值保留）。
- 不可变集合行为（CompositeType.keySet / TabularType.getIndexNames / CompositeDataSupport.values /
  TabularDataSupport.keySet 的 add → UnsupportedOperationException）。
- 构造路径等价与规范细节（ArrayType(SimpleType,boolean) 两分支、嵌套 primitive 维度相加、
  TabularType 重复索引名允许、CompositeType 0 字段不允许与 trim、描述不参与 CompositeType.equals、
  CompositeDataSupport.equals 要求同实现类）。
- CompositeDataView 经本地 MBeanServer 的 MXBean 真闭环；CompositeDataInvocationHandler 的
  Object 方法路由与全部精确消息负例。
- TabularDataSupport Map 视图语义（键为索引 List、Map.put 返回所放行 5090566、
  containsKey 吞 CCE vs get/remove 抛 CCE、无 3 参便捷 put、null type 构造期拒绝）。
- javap 双侧对照：11 类公开 API 面完全一致；异常消息逐字一致（除 JDK-27 NPE/CCE helpful 文本、
  JDK-9+ InaccessibleObjectException 两处，均按类型/双路径断言并记录）。
