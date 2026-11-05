# org.omg.PortableServer.Servant 契约覆盖设计（仅 JDK-8）

## API 功能
服务端 servant 基类（本地 abstract）：`_all_interfaces` 唯一抽象方法；7 个 final 委托通道
（_get_delegate/_set_delegate/_this_object×2/_orb/_poa/_object_id）+ _default_POA/_is_a/_non_existent/_get_interface_def。

## 使用方式
业务实现继承 Servant（或 DynamicImplementation）；ORB 通过 `portable.Delegate` 注入行为；
`_this_object(orb)` 换取对象引用。

## 参考来源
- OMG POA IDL-to-Java 映射；jdk8u372-b07 实测 + javap（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. 未关联 ORB：9 个通道全部 `BAD_INV_ORDER` minor==0，消息精确
   "The Servant has not been associated with an ORB instance"。
2. 自定义 Delegate：8 通道全路由（调用顺序日志 == 预期序列）；`_set_delegate(null)` 回到未关联态。
3. **离线闭环（关键）**：`_this_object(orb)` 立即装上 RootPOA 委托——返回 IOR 引用、`_orb()==orb`、
   `_poa()==_default_POA()` toString 精确 `POA[ObjectAdapterID[RootPOA], uniquePOAId=0, state=RUN, invocationCount=0]`、
   delegate 类名 `com.sun.corba.se.impl.oa.poa.DelegateImpl`、`_this_object()` 每次新引用。
4. **易错**：`_this_object(orb)` 会**覆盖**自定义 delegate；已关联未激活时 _object_id/_is_a/_non_existent
   → `OBJ_ADAPTER` minor=0x535503FD，_get_interface_def → `NO_IMPLEMENT` minor=0x535503EA（会打 WARNING，测试静音 stderr）。
5. 形态：12 声明方法（7 final + 4 普通 + 1 抽象）、public 无参构造器、直接继承 Object。
