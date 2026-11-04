# javax.swing.text.html.parser.ContentModel — DESIGN

## 1. API 功能
内容模型（受限 BNF 表达式）的表示（`implements Serializable`）。公开字段 `type/content/next`；type 为 `'*' '?' '+' ',' '|' '&'` 字符或 0（叶子元素）。
- 构造器：`ContentModel()`、`ContentModel(Element)`、`ContentModel(int, ContentModel)`、`ContentModel(int, Object, ContentModel)`。
- 查询：`empty()`（能否匹配空流）、`getElements(Vector<Element>)`（收集元素）、`first(Object)`（token 是否可为首）、`first()`（返回必然的首元素）、`toString()`。

## 2. Java 用户如何使用
从 DTD 的 `Element.getContent()` 取得内容模型，递归调用 `first()/first(token)` 判断可匹配的首元素，`getElements` 收集子元素，`empty()` 判断空匹配；也可自行 `new ContentModel('*', inner)` 组合。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DTD.java`（自用）：
  ```java
  protected ContentModel defContentModel(int type, Object obj, ContentModel next) {
      return new ContentModel(type, obj, next);
  }
  ```
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java`（自用）：`makeTag` 前用 `elem.content.first(...)`/`content.getElements(...)` 判断标签合法性。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ContentModel.java`（自用）：`empty()`/`first()` 按 type 分派（`'*' '?'` 空、`'+' ','` 取 content.first() 等）。

## 4. 测试用例设计（黑盒部分，`ContentModelTest.testBlackBox`）
1. 默认构造器：type/content/next 默认、empty()==false、first()==null。
2. 元素构造器：content==element、first()==element、first(b)==true、first(i)==false、toString==name。
3. `'*' '?' '+'`：empty/first 语义（`*`/`?` 空、`+` 非空且 first==b）。
4. `','` 链表组合：`(b , i)` 的 empty()、first()==b、first(b)/first(i)、getElements 顺序、toString。

## 5. 深化批（契约覆盖增量）
1. 六种运算符语义矩阵：`*`/`?` empty()==true、first()==null；`+`/`,`/`|`/`&` empty()==false；`|`/`&` first()==null（歧义）而 first(member)==true；toString 精确表 `b*`/`b?`/`b+`/`(b , i)`/`(b | i)`/`(b & i)`/单成员 `(b)`；三元素链 `(b , i , u)`。
2. type=0 + Element 内容：toString==元素名、first()==元素、getElements 收集；type=0 + String 内容：toString 正常、first() 抛 CCE（实测）。
3. html32 真实内容模型精确串：html=`(head , body , plaintext?)`、head=`(title? & isindex? & base? & nextid?)`、table=`(tr | caption)*`；br(EMPTY) content==null。
4. 序列化：serialVersionUID=-1130825523866321257（两版一致）+ round-trip。
