# EndpointContext 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.EndpointContext 是 JAX-WS 2.2 新增抽象类（**非接口**）：为一组相关 Endpoint 提供共享上下文容器——getEndpoints() 返回同组端点集合，容器借此协调组内端点（典型场景：同一 WAR/EAR 内端口共享 stateful 服务实例）。经 Endpoint.setEndpointContext(EndpointContext) 关联。

## 2. 使用方式
典型路径：`EndpointContext ctx = new MyContext(endpoints); endpointA.setEndpointContext(ctx); endpointB.setEndpointContext(ctx);`。本测试：结构面 + SimpleContext（固定 Set）getEndpoints 回读 + 真实双端点 wiring + 组内独立生命周期（localhost:0 发布/停止矩阵）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/EndpointContext.html）、JSR 224 (JAX-WS 2.2) Maintenance Release、本仓库 EndpointContextTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：**抽象类**（isAbstract）非接口；恰 1 抽象方法 getEndpoints()→Set<Endpoint>；public 无参构造供子类 super 链；直接 newInstance 失败（InstantiationException）。
- 自造实现闭环：空集合/不可变集合/同一 Set 引用回读/null 集合宽容/跨调用引用稳定。
- **真实 wiring（P0）**：两个 @WebService impl → Endpoint.create → 注入共享 SimpleContext（setEndpointContext 接受、null 也接受）；context 集合 contains 两端点、迭代计数 2。
- 组内生命周期矩阵：ctx 内 c1 publish → c1 published/c2 未发布；c2 publish → 双发布；遍历 getEndpoints 全部 stop → 全部未发布（localhost:0 ephemeral，立即停）。
- 关联面：Endpoint.setEndpointContext(EndpointContext) public 签名锁定。
- 坑：JDK-8 的 EndpointContext 在 javax.xml.ws 根包（不是 spi 包）；abstractCount 判定勿与继承的 Endpoint 方法混淆。
