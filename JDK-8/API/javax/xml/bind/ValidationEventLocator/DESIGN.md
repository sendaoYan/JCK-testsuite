# javax.xml.bind.ValidationEventLocator 契约覆盖设计（JDK-8）

## 1. API 功能
校验事件的定位信息接口：六个只读 getter（getURL/getOffset/getLineNumber/getColumnNumber/getObject/getNode），分别对应"事件发生在哪个源、哪个偏移、哪行哪列、哪个被绑对象、哪个 DOM 节点"。具体实现是 helpers 包的 ValidationEventLocatorImpl；接口各字段可为 null/-1 表示"未知"。

## 2. 使用方式
```java
u.setEventHandler(e -> {
    ValidationEventLocator l = e.getLocator();
    int line = l.getLineNumber();   // -1 表示未知
    return true;
});
```
真实通道（本测试实证）：Reader 源的意外元素事件 locator 为 line=1、col=47、offset=-1、url/object/node 全 null。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/ValidationEventLocator.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/ValidationEventLocator.java`
- 本仓库测试：`ValidationEventLocatorTest.java`

## 4. 测试用例设计 + 易错规范细节
- 签名矩阵：恰好 6 个声明方法、返回类型逐一对号（URL/int/int/int/Object/org.w3c.dom.Node）、无父接口。
- 默认态锁定（经 ValidationEventLocatorImpl 实例、以接口类型读回）：url/object/node null，**offset/line/column 均为 -1 而非 0**（任务简报原假设是 0，实测纠正）。
- 六个 setter 的回读全部通过"接口类型引用"完成（具体 impl 只在赋值时出现）。
- 真实 locator：意外元素事件 line==1、col==47（固定 XML 串锁定）、offset==-1、url null（Reader 源无 URL）。
- 易错点：DOM 通道才填 node/object；SAX 通道只填 line/column；断言默认值时勿写 0。
