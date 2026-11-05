# javax.management.openmbean.OpenMBeanAttributeInfo — 测试设计

## 1. API 功能
开放 MBean 属性元数据接口：在 OpenMBeanParameterInfo 之上增加可读/可写/is-is 三布尔。
方法集 18 个 = 参数 15 个 + isReadable/isWritable/isIs。

## 2. Java 用户典型用法
new OpenMBeanAttributeInfoSupport(name, desc, openType, readable, writable, isIs[, 约束...])，
以接口引用传给 OpenMBeanInfoSupport；客户端据 isReadable/isWritable 渲染；isIs 专指
boolean 型 “isXxx()” getter。属性接口可直接当参数视图用（子接口赋值兼容）。

## 3. 参考来源
- Oracle javadoc：javax.management.openmbean.OpenMBeanAttributeInfo。
- OpenJDK jdk8u/jdk25u 源码 OpenMBeanAttributeInfo.java（接口定义与注释语义）。
- 双侧 javap 实测。

## 4. 测试用例设计 + 易错细节（OpenMBeanAttributeInfoTest，批 G 深化）
- 结构：精确方法集 18 个（含 isReadable/0、isWritable/0、isIs/0）；无公开字段；
  extends OpenMBeanParameterInfo。
- 契约：读写 isIs 三布尔经接口引用取真值；isIs 形态（readable-only boolean）；legal 形态；
  **易错：equals 参与三布尔**（只改 readable 即不等）；属性对象赋给 OpenMBeanParameterInfo
  引用后 getOpenType 仍可用（子接口视图）。
- 版本差异：两侧一致。
