# javax.swing.text.html.parser.Entity — DESIGN

## 1. API 功能
DTD 中实体（ENTITY 构造）的描述（`implements DTDConstants`）。公开字段 `name/type/data`。
- 构造器：`Entity(String name, int type, char[] data)`。
- 查询：`getName()/getType()/isParameter()/isGeneral()/getData()/getString()`。
- 静态：`name2type(String)`（PUBLIC/CDATA/SDATA/PI/STARTTAG/ENDTAG/MS/MD/SYSTEM，未知回退 CDATA）。

## 2. Java 用户如何使用
`DTD.getEntity(name)` 或 `getEntity(int)` 拿实体，`getString()` 得到展开文本（如 `amp -> "&"`）；`isGeneral()/isParameter()` 区分通用/参数实体，`getType()` 返回去标志位后的类型；也可直接 `new Entity(name, GENERAL|CDATA, data)`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DTD.java`（自用）：
  ```java
  public Entity defEntity(String name, int type, int ch) {
      char[] data = {(char)ch};
      return defineEntity(name, type, data);
  }
  // defineEntity: new Entity(name, type, data); entityHash.put(name, ent);
  // 单字符 GENERAL 实体还会以 Integer.valueOf(data[0]) 建立字符索引
  ```
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Entity.java`（自用）：`getType()` 返回 `type & 0xFFFF`，`isParameter()` 判 `PARAMETER` 位。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java`（自用）：解析 `&name;` 时查 `dtd.getEntity(name)` 展开。

## 4. 测试用例设计（黑盒部分，`EntityTest.testBlackBox`）
1. `name2type`：PUBLIC/CDATA/SDATA/STARTTAG/ENDTAG 精确 + 未知回退 CDATA。
2. html32 实体：`amp`->"&"、`gt`->">"、`lt`->"<"，type=CDATA、isGeneral、!isParameter、getData 长度；未知实体 null。
3. 构造器：GENERAL|CDATA 实体的 getType 掩码、isGeneral/isParameter、getString/getData 引用。
4. 参数实体：PARAMETER|CDATA 的 isParameter=true、isGeneral=false。
