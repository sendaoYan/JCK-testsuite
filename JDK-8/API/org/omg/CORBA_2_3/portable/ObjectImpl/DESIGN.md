# org.omg.CORBA_2_3.portable.ObjectImpl 契约设计

## API 功能
2.3 对象实现基类：新增 _get_codebase()。

## 参考来源
- OMG 2.3 Java 映射；jdk8u372-b07 实测。

## 测试用例设计
- abstract、super==portable.ObjectImpl、恰 1 方法 _get_codebase()→String、public 实例方法。
- 真实用法：org.omg.stub 的 JMX Tie 类继承本类。

## 易错规范细节
- _get_codebase 带下划线前缀（ORB 运行时约定，非 JavaBean 命名）。
