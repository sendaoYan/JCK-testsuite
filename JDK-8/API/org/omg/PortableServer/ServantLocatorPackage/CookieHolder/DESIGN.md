# org.omg.PortableServer.ServantLocatorPackage.CookieHolder 契约覆盖设计（仅 JDK-8）

## API 功能
preinvoke 的 cookie 出参 Holder：`public Object value`；实现 Streamable。

## 使用方式
servant 经 Holder 填 cookie，postinvoke 收回；本 JDK 的 Streamable 三方法是未实现桩。

## 参考来源
- OMG POA cookie；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. final 类、恰 2 构造器（默认 null / 按引用存）；value 公开可变可空。
2. **关键负例**：`_type()/_write(os)/_read(is)` 全部 `org.omg.CORBA.NO_IMPLEMENT` minor==0
   （JDK-8 未实现 Streamable 通道——cookie 在本机内按引用传，不走 wire）；失败调用不改动 value。
