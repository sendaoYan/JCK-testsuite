# org.omg.CORBA_2_3.portable.Delegate 契约设计

## API 功能
2.3 代理基类：新增 get_codebase(org.omg.CORBA.Object)。

## 参考来源
- OMG 2.3 Java 映射；jdk8u372-b07 实测。

## 测试用例设计
- abstract、super==portable.Delegate、恰 1 方法、返回 String、具体方法（非 abstract）。
