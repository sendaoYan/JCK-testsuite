# org.omg.PortableServer.POAPackage.InvalidPolicy 契约覆盖设计（仅 JDK-8）

## API 功能
final UserException（IDL exception）：()/(short)/(String,short)；repo id `IDL:omg.org/PortableServer/POA/InvalidPolicy:1.0`。create_POA 策略数组中非法策略的位置信号（index 从 0 计）。

## 使用方式
PortableServer 相关操作的 checked 异常信号；Helper 提供 Any/CDR 通道（见同包 Helper 用例）。

## 参考来源
- OMG PortableServer IDL；jdk8u372-b07 实测（/tmp/omg-proto-e）

## 测试用例设计 + 易错细节
1. 构造矩阵 + toString 三形态精确（`FQCN: repoId[  两空格+msg]`）；**getMessage 前缀是 repo id**。
2. serialVersionUID 精确 3204212102282117205 + Java 序列化往返保消息。
3. final、IDLEntity、checked（已有断言保留）。
4. InvalidPolicy 专有：`public short index` 三构造器回读、负数/MIN/MAX 边界、public 可变、序列化保真。