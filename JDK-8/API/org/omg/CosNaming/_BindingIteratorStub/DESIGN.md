# org.omg.CosNaming._BindingIteratorStub

## API 功能
客户端桩：把命名接口调用编码为 GIOP 请求。
## 使用方式
`new ...Stub()` + ORB 注入 delegate 后可用；`_ids()` 声明类型。
## 参考来源
JDK-8 rt.jar；实测 jdk8u372-b07。
## 测试用例设计 + 易错细节
- `new` 不抛；_ids() **单元素且逐字精确** `IDL:omg.org/CosNaming/BindingIterator:1.0`。
- 无 delegate（离线）时任何远程操作 → org.omg.CORBA.BAD_OPERATION（本地即抛，无需网络）——next_one/next_n/destroy 实证。
