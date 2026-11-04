# java.beans.beancontext.BeanContextSupport.BCSChild — 设计案

## 1. API 功能
BCSChild 嵌套类（implements Serializable），内部保存子 Bean 与代理 peer（包私有构造器）。

## 2. Java 用户如何使用
不直接使用；BeanContextSupport.add 时内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：public + Serializable + 非 public 构造器。黑盒（`testBlackBox`）：通过子类暴露 bcsChildren() 间接取得 BCSChild 实例。

## 5. 深化（batch beansH）—— 契约级覆盖追加
- **删除了测试文件中错误的 `@key headful` 行**（纯 headless 测试，理由同 BeanContextSupport 案）。
- 断言数 8 → 40。新增：完整反射结构断言（protected 非 static 内部类、唯一包私有 3 参构造器 (BCS,Object,Object)、declared methods 恰 5 个包私有访问器、字段 child/proxyPeer/removePending、serialVersionUID -5815286101609939109L）；BeanContextProxy 子项的包装对行为（size==2、双元素事件、proxy 子 BeanContext 接线、成对删除、null getBeanContextProxy → NPE）；序列化往返存活（反序列化副本按类型断言）。
- JDK-27 与 JDK-8 结构/行为完全一致（javap + 探针双确认）。
