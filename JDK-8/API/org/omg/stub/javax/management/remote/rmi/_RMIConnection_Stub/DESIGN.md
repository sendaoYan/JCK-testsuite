# _RMIConnection_Stub 契约设计

## API 功能
JMX RMIConnection 的 RMI-IIOP 桩。

## 参考来源
- javax.management.remote.rmi 映射；jdk8u372-b07 实测。

## 测试用例设计
- new 不抛；_ids() 恰 1 元素，精确 "RMI:javax.management.remote.rmi.RMIConnection:0000000000000000"（RMI: 前缀 + 零 suID hash）。
- implements RMIConnection；非 final；toString 前缀。

## 易错规范细节
- id 是 RMI: 格式而非 IDL:omg.org 格式；hash 段全零。
