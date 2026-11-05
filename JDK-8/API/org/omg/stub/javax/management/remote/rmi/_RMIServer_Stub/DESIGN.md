# _RMIServer_Stub 契约设计

## API 功能
JMX RMIServer 的 RMI-IIOP 桩。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- new 不抛；_ids() 精确 "RMI:javax.management.remote.rmi.RMIServer:0000000000000000"；implements RMIServer。
