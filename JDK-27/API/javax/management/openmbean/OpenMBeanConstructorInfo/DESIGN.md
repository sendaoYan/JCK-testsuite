# javax.management.openmbean.OpenMBeanConstructorInfo 设计案

## 1. API 功能
开放 MBean 构造器信息接口：getName/getDescription/getSignature。

## 2. Java 用户如何使用
由 OpenMBeanConstructorInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanConstructorInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 Support 构造+签名查询。

## 深化（2026-11，批 G）
- 精确方法集 6 个；无公开字段。
- getSignature 元素是 OpenMBeanParameterInfo；null 签名归一化长度 0；equals 只看 name+signature。
- 版本差异：零差异。
