# org.omg.PortableServer.POAPackage.ServantNotActiveHelper 契约覆盖设计（仅 JDK-8）

## API 功能
IDL Helper（全静态）：id/type/insert/extract/read/write；为 org.omg.PortableServer.POAPackage.ServantNotActive 提供 Any 与 CDR 通道。

## 使用方式
`insert(any, ex)`/`extract(any)` Any 往返；`write(os, ex)`/`read(is)` CDR 往返；id() 即 repo id。

## 参考来源
- OMG IDL-to-Java Helper 规范；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. id() 精确 `IDL:omg.org/PortableServer/POA/ServantNotActive:1.0`；type().kind()==tk_except 且 type().id()==id()。
2. Any insert/extract 往返保类；**Any/CDR 往返都丢用户消息**（IDL exception wire 不传 reason）。
3. 同流背靠背两个异常顺序保真。
4. extract(empty Any) → MARSHAL。
