# org.omg.CORBA_2_3.ORB 契约设计

## API 功能
2.3 扩展 ORB：值工厂注册/查找/注销 + get_value_def + set_delegate，共 5 方法。

## 参考来源
- OMG CORBA 2.3 / Java-to-IDL 规范；jdk8u372-b07 javap+反射实测。不实例化。

## 测试用例设计
- abstract、extends org.omg.CORBA.ORB、恰 5 方法名集合精确。
- register_value_factory(String,ValueFactory)→ValueFactory；get_value_def(String)→Object throws BAD_PARAM。
- 单 public 无参构造。

## 易错规范细节
- 绝不 new（抽象）；反射锁方法面即可。
