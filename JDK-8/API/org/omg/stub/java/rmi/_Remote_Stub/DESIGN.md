# org.omg.stub.java.rmi._Remote_Stub 契约设计

## API 功能
java.rmi.Remote 的 RMI-IIOP 桩（final，extends javax.rmi.CORBA.Stub）。

## 参考来源
- Java-to-IDL 规范；jdk8u372-b07 实测。

## 测试用例设计
- new 不抛；_ids() 恰 1 元素且为**空字符串**（Remote 无 CORBA rep id）。
- equals(null)=false、equals(self)=true；hashCode() 无 delegate → BAD_OPERATION；toString 精确 "<fqcn>: no delegate set"；Serializable。

## 易错规范细节
- 别断言 ids 非空字符串——实测为 ""。
- hashCode 会抛（依赖 delegate），必须 try/catch。
