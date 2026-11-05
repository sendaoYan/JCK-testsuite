# org.omg.PortableServer.POAManagerPackage.AdapterInactive 契约覆盖设计（仅 JDK-8）

## API 功能
final UserException（IDL exception）：()/(String)；repo id `IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0`。INACTIVE 态 POAManager 上的状态迁移信号

## 使用方式
PortableServer 相关操作的 checked 异常信号；Helper 提供 Any/CDR 通道（见同包 Helper 用例）。

## 参考来源
- OMG PortableServer IDL；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. 构造矩阵 + toString 三形态精确（`FQCN: repoId[  两空格+msg]`）；**getMessage 前缀是 repo id**。
2. serialVersionUID 精确 6945182851085567131 + Java 序列化往返保消息。
3. final、IDLEntity、checked（已有断言保留）。
