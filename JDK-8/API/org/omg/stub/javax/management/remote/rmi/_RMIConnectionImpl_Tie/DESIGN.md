# _RMIConnectionImpl_Tie 契约设计

## API 功能
JMX RMIConnection 的服务端 Tie（javax.rmi.CORBA.Tie，extends CORBA_2_3.portable.ObjectImpl）。

## 参考来源
- Java-to-IDL；jdk8u372-b07 实测。

## 测试用例设计
- new 不抛；_ids() 与对应 Stub 同 id（RMI:...RMIConnection:0000000000000000）。
- implements javax.rmi.CORBA.Tie；super==org.omg.CORBA_2_3.portable.ObjectImpl；Tie.target() 返回 java.rmi.Remote。

## 易错规范细节
- Tie 与 Stub 共享同一 rep id；Tie 走 2.3 portable 层。
