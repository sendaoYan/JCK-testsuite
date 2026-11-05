# javax.xml.bind.helpers.ValidationEventLocatorImpl 契约覆盖设计（JDK-8）

## 1. API 功能
ValidationEventLocator 的默认实现：5 个构造器（无参/Locator/SAXParseException/Node/Object）+ 6 组 setter/getter。不同来源构造时自动搬运对应字段（SAX→url/line/column、DOM→node、绑定对象→object）。**无参默认的数值字段是 -1 而不是 0。**

## 2. 使用方式
```java
ValidationEventLocatorImpl l = new ValidationEventLocatorImpl();
l.setURL(url); l.setLineNumber(3); l.setColumnNumber(9);
ValidationEventImpl e = new ValidationEventImpl(1, "msg", l);
// SAX 侧自动搬运：
l = new ValidationEventLocatorImpl(saxParseException);
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/ValidationEventLocatorImpl.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/ValidationEventLocatorImpl.java`
- 本仓库测试：`ValidationEventLocatorImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 默认态（实测锁定）：url/object/node null，**offset/line/column 全部 -1**（不是 0——SAX 惯例"未知"哨兵）。
- setter 全矩阵：6 组 set/get 同实例回读、URL 可替换可置 null、object 可置 null。
- Locator 构造：systemId `http://...` → getURL 精确转换；line/column 原样搬运；**空 LocatorImpl（SAX 默认 0/0）→ url null、line/column 保持 0**（与"未知 -1"语义并存，实测锁定）；object/node/offset 不动。
- SAXParseException 构造：同上搬运规则；systemId null → url null、line/column -1 保留。
- Node 构造：node 同实例、其余不动；Object 构造：object 同实例、node null。
- 负例：`Locator(null)`/`SAXParseException(null)` → IllegalArgumentException（实测锁定）。
- toString 锁定：默认 `[node=null,object=null,url=null,line=-1,col=-1,offset=-1]`；填充后含 `url=.../line=N/col=N/offset=N`，node 显示为 `[el ...` 形式。
- 易错点：5 个公共构造器计数断言；line/col 默认值勿写 0。
