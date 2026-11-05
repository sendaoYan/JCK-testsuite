# javax.management.openmbean.OpenMBeanAttributeInfo 设计案

## 1. API 功能
开放 MBean 属性信息接口（extends OpenMBeanParameterInfo）：isReadable/isWritable/isIs。

## 2. Java 用户如何使用
由 OpenMBeanAttributeInfoSupport 实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/java.management/share/classes/javax/management/openmbean/OpenMBeanAttributeInfo.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 OpenMBeanAttributeInfoSupport 构造+查询。

## 深化（2026-11，批 G）
- 精确方法集 18 个（+isReadable/isWritable/isIs）；无公开字段；extends OpenMBeanParameterInfo。
- 三布尔经接口引用取真值；isIs 形态（readable-only boolean）；equals 参与三布尔。
- 属性对象可直接当 OpenMBeanParameterInfo 视图用（子接口赋值兼容）。
- 版本差异：零差异。
