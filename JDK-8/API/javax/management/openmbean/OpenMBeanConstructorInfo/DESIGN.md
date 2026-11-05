# javax.management.openmbean.OpenMBeanConstructorInfo — 测试设计

## 1. API 功能
开放 MBean 构造器元数据接口。方法集 6 个：getName、getDescription、getSignature、equals、
hashCode、toString（getSignature 继承自 MBeanConstructorInfo 面）。

## 2. Java 用户典型用法
new OpenMBeanConstructorInfoSupport(name, desc, params)（params 为 OpenMBeanParameterInfo[]，可为
null → 空签名）放入 OpenMBeanInfoSupport；客户端展示类有哪些构造形态。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenMBeanConstructorInfo。
- OpenJDK 源码同名接口；双侧 javap 实测。

## 4. 测试用例设计 + 易错细节（OpenMBeanConstructorInfoTest，批 G 深化）
- 结构：精确方法集 6 个；无公开字段。
- 契约：getSignature 经接口引用返回 OpenMBeanParameterInfo 元素；null 签名归一化为长度 0；
  equals 只看 name + signature（desc 不参与）。
- 版本差异：两侧一致。
