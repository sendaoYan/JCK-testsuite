# Guard — 设计案

## 1. API 功能
单方法接口：`void checkGuard(Object object) throws SecurityException`。Permission 实现本接口（平台标准 Guard）；GuardedObject 用它守卫目标对象。

## 2. 使用方式
实现方决定放行（正常返回）或拒绝（抛 SecurityException）；GuardedObject.getObject 每次调用前都会咨询 guard。

## 3. 参考来源
- javadoc `java.security.Guard` / `GuardedObject` / `Permission.checkGuard`。
- 双侧探针实测（throws 子句、checkGuard 实参、无 SM 行为）。

## 4. 测试用例设计 + 易错规范细节
- 结构：单方法接口、checkGuard(Object)→void、**throws 子句声明了 SecurityException**（ unchecked 但显式声明——反射 getExceptionTypes 恰 1 项）。
- 行为：记录型 guard 断言实参身份透传（null 目标也允许）；拒绝时 SecurityException 消息逐字传播；非 Security 的 unchecked 异常（ISE）原样传播不包装。
- Permission 集成：Permission instanceof Guard。
- **Permission.checkGuard 无 SecurityManager 时是 no-op（JDK-8 实测；仅 SM 存在才委托）**；对照 AccessController.checkPermission(未授权 SecurityPermission) 无 SM 仍抛 AccessControlException 并携带该权限——两者行为不同，分别断言。
- GuardedObject 联动：每次 getObject 都咨询（计数 3 次）；checkGuard 收到的是**被守卫对象本身**（JDK-8 实现），不是 GuardedObject。
- 易错点：checkGuard 的实参在 getObject 路径与 writeObject 路径下可能不同实现传不同值，只断言可调用与放行/拒绝语义。
