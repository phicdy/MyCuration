package com.phicdy.mycuration.domain.rss

class RssV1 {
    fun text(): String {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rdf:RDF\n" +
                " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                " xmlns=\"http://purl.org/rss/1.0/\"\n" +
                " xmlns:content=\"http://purl.org/rss/1.0/modules/content/\"\n" +
                " xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\"\n" +
                " xmlns:opensearch=\"http://a9.com/-/spec/opensearchrss/1.0/\"\n" +
                " xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
                " xmlns:hatena=\"http://www.hatena.ne.jp/info/xmlns#\"\n" +
                " xmlns:media=\"http://search.yahoo.com/mrss\"\n" +
                ">\n" +
                "  <channel rdf:about=\"http://b.hatena.ne.jp/hotentry/it\">\n" +
                "    <title>はてなブックマーク - 人気エントリー - テクノロジー</title>\n" +
                "    <link>http://b.hatena.ne.jp/hotentry/it</link>\n" +
                "    <description>最近の人気エントリー - テクノロジー</description>\n" +
                "\n" +
                "    <items>\n" +
                "      <rdf:Seq>\n" +
                "        <rdf:li rdf:resource=\"https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908\" />\n" +
                "        <rdf:li rdf:resource=\"https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html\" />\n" +
                "        <rdf:li rdf:resource=\"https://kantan-shikaku.com/ks/excel-test-for-work/\" />\n" +
                "        <rdf:li rdf:resource=\"http://www.kodansha.co.jp/upload/pr.kodansha.co.jp/files/180413_seimei_kaizokuban.pdf\" />\n" +
                "        <rdf:li rdf:resource=\"https://anond.hatelabo.jp/20180413102948\" />\n" +
                "        <rdf:li rdf:resource=\"https://employment.en-japan.com/engineerhub/entry/2018/04/13/110000\" />\n" +
                "        <rdf:li rdf:resource=\"https://employment.en-japan.com/engineerhub/entry/2018/04/10/110000\" />\n" +
                "        <rdf:li rdf:resource=\"https://qiita.com/issei_y/items/ab641746be2704db98be\" />\n" +
                "        <rdf:li rdf:resource=\"https://withnews.jp/article/f0180413000qq000000000000000W06w10101qq000017106A\" />\n" +
                "        <rdf:li rdf:resource=\"https://qiita.com/aoshirobo/items/32deb45cb8c8b87d65a4\" />\n" +
                "        <rdf:li rdf:resource=\"https://axia.co.jp/2018-04-13\" />\n" +
                "        <rdf:li rdf:resource=\"http://kyon-mm.hatenablog.com/entry/2018/04/13/074023\" />\n" +
                "        <rdf:li rdf:resource=\"https://togetter.com/li/1217505\" />\n" +
                "        <rdf:li rdf:resource=\"https://virtualcast.jp/\" />\n" +
                "        <rdf:li rdf:resource=\"https://liginc.co.jp/388554\" />\n" +
                "        <rdf:li rdf:resource=\"https://blog.tinect.jp/?p=50813\" />\n" +
                "        <rdf:li rdf:resource=\"https://pc.watch.impress.co.jp/docs/news/1116988.html\" />\n" +
                "        <rdf:li rdf:resource=\"https://www.jaipa.or.jp/information/docs/180412-1.pdf\" />\n" +
                "        <rdf:li rdf:resource=\"https://natalie.mu/comic/news/277927\" />\n" +
                "        <rdf:li rdf:resource=\"https://inside.dmm.com/entry/2018/04/13/hello-kubernetes\" />\n" +
                "        <rdf:li rdf:resource=\"http://uxmilk.jp/62899\" />\n" +
                "        <rdf:li rdf:resource=\"https://postd.cc/vim3/\" />\n" +
                "        <rdf:li rdf:resource=\"https://linq.career-tasu.jp/magazine/knowhow-excel-test/\" />\n" +
                "        <rdf:li rdf:resource=\"https://vimawesome.com/\" />\n" +
                "        <rdf:li rdf:resource=\"https://anond.hatelabo.jp/20180413022546\" />\n" +
                "        <rdf:li rdf:resource=\"http://nlab.itmedia.co.jp/nl/articles/1804/13/news091.html\" />\n" +
                "        <rdf:li rdf:resource=\"http://vgdrome.blogspot.com/2018/04/chinesePUBGripoffwar.html\" />\n" +
                "        <rdf:li rdf:resource=\"https://codezine.jp/article/detail/10734\" />\n" +
                "        <rdf:li rdf:resource=\"https://webtan.impress.co.jp/e/2018/04/13/28947\" />\n" +
                "        <rdf:li rdf:resource=\"https://gigazine.net/news/20180413-facial-recognition-catch-fugitive/\" />\n" +
                "      </rdf:Seq>\n" +
                "    </items>\n" +
                "  </channel>\n" +
                "  <item rdf:about=\"https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908\">\n" +
                "    <title>トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog</title>\n" +
                "    <link>https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908</link>\n" +
                "    <description>2018 - 04 - 13 トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 〜〜が知っておくべきサイト20選とか、エンジニアなら今すぐフォローすべき有名人とか、いつも釣られてみにいくと全く興味なかったり拍子抜けしたりするわけだが、こういうのが並んでいたらあまりの格の違いに絶望してしまうだろうというものを適当に並べてみた。私が見ているわけではなくて、こうありた...</description>\n" +
                "    <content:encoded>&lt;blockquote cite=&quot;https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot; title=&quot;トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&quot;&gt;&lt;cite&gt;&lt;img src=&quot;http://cdn-ak.favicon.st-hatena.com/?url=https%3A%2F%2Fkuenishi.hatenadiary.jp%2Fentry%2F2018%2F04%2F13%2F022908&quot; alt=&quot;&quot; /&gt; &lt;a href=&quot;https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot;&gt;トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&lt;/a&gt;&lt;/cite&gt;&lt;p&gt;&lt;a href=&quot;https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot;&gt;&lt;img src=&quot;https://cdn-ak-scissors.b.st-hatena.com/image/square/1db2a014bdea7ae25967aced483f3e5eca394182/height=90;version=1;width=120/https%3A%2F%2Fcdn.blog.st-hatena.com%2Fimages%2Ftheme%2Fog-image-1500.png&quot; alt=&quot;トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&quot; title=&quot;トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&quot; class=&quot;entry-image&quot; /&gt;&lt;/a&gt;&lt;/p&gt;&lt;p&gt;2018 - 04 - 13 トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 〜〜が知っておくべきサイト20選とか、エンジニアなら今すぐフォローすべき有名人とか、いつも釣られてみにいくと全く興味なかったり拍子抜けしたりするわけだが、こういうのが並んでいたらあまりの格の違いに絶望してしまうだろうというものを適当に並べてみた。私が見ているわけではなくて、こうありた...&lt;/p&gt;&lt;p&gt;&lt;a href=&quot;http://b.hatena.ne.jp/entry/https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot;&gt;&lt;img src=&quot;http://b.hatena.ne.jp/entry/image/https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot; alt=&quot;はてなブックマーク - トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&quot; title=&quot;はてなブックマーク - トップレベルのコンピュータエンジニアなら普段からチェックして当然の技術系メディアN選 - kuenishi's blog&quot; border=&quot;0&quot; style=&quot;border: none&quot; /&gt;&lt;/a&gt; &lt;a href=&quot;http://b.hatena.ne.jp/append?https://kuenishi.hatenadiary.jp/entry/2018/04/13/022908&quot;&gt;&lt;img src=&quot;http://b.hatena.ne.jp/images/append.gif&quot; border=&quot;0&quot; alt=&quot;はてなブックマークに追加&quot; title=&quot;はてなブックマークに追加&quot; /&gt;&lt;/a&gt;&lt;/p&gt;&lt;/blockquote&gt;</content:encoded>\n" +
                "    <dc:date>2018-04-13T02:33:56+09:00</dc:date>\n" +
                "    <dc:subject>テクノロジー</dc:subject>\n" +
                "    <hatena:bookmarkcount>1902</hatena:bookmarkcount>\n" +
                "  </item>\n" +
                "  <item rdf:about=\"https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html\">\n" +
                "    <title>「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch</title>\n" +
                "    <link>https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html</link>\n" +
                "    <description>やじうまWatch 「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中 tks24 2018年4月13日 06:00 　入社試験に使える、Excelがどの程度使えるかをチェックするためのサンプル問題が公開され、有用だと話題になっている。 　「Excelが使える」と自己アピールする人の中には、マクロまでしっかり使いこなす人もいれば、セルに数字を入力するので精一杯の人...</description>\n" +
                "    <content:encoded>&lt;blockquote cite=&quot;https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot; title=&quot;「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&quot;&gt;&lt;cite&gt;&lt;img src=&quot;http://cdn-ak.favicon.st-hatena.com/?url=https%3A%2F%2Finternet.watch.impress.co.jp%2Fdocs%2Fyajiuma%2F1116878.html&quot; alt=&quot;&quot; /&gt; &lt;a href=&quot;https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot;&gt;「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&lt;/a&gt;&lt;/cite&gt;&lt;p&gt;&lt;a href=&quot;https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot;&gt;&lt;img src=&quot;https://cdn-ak-scissors.b.st-hatena.com/image/square/0e82d1dcac71fcd16db3e39d9fd4e11018bfc6ea/height=90;version=1;width=120/https%3A%2F%2Finternet.watch.impress.co.jp%2Fimg%2Fiw%2Flist%2F1116%2F878%2Fyajiuma-watch_1.png&quot; alt=&quot;「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&quot; title=&quot;「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&quot; class=&quot;entry-image&quot; /&gt;&lt;/a&gt;&lt;/p&gt;&lt;p&gt;やじうまWatch 「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中 tks24 2018年4月13日 06:00 　入社試験に使える、Excelがどの程度使えるかをチェックするためのサンプル問題が公開され、有用だと話題になっている。 　「Excelが使える」と自己アピールする人の中には、マクロまでしっかり使いこなす人もいれば、セルに数字を入力するので精一杯の人...&lt;/p&gt;&lt;p&gt;&lt;a href=&quot;http://b.hatena.ne.jp/entry/https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot;&gt;&lt;img src=&quot;http://b.hatena.ne.jp/entry/image/https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot; alt=&quot;はてなブックマーク - 「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&quot; title=&quot;はてなブックマーク - 「Excelが使える」のレベルを的確に見抜ける、入社試験に使えるサンプル問題が公開中【やじうまWatch】 - INTERNET Watch&quot; border=&quot;0&quot; style=&quot;border: none&quot; /&gt;&lt;/a&gt; &lt;a href=&quot;http://b.hatena.ne.jp/append?https://internet.watch.impress.co.jp/docs/yajiuma/1116878.html&quot;&gt;&lt;img src=&quot;http://b.hatena.ne.jp/images/append.gif&quot; border=&quot;0&quot; alt=&quot;はてなブックマークに追加&quot; title=&quot;はてなブックマークに追加&quot; /&gt;&lt;/a&gt;&lt;/p&gt;&lt;/blockquote&gt;</content:encoded>\n" +
                "    <dc:date>2018-04-13T06:03:55+09:00</dc:date>\n" +
                "    <dc:subject>テクノロジー</dc:subject>\n" +
                "    <hatena:bookmarkcount>813</hatena:bookmarkcount>\n" +
                "  </item>\n" +
                "</rdf:RDF>\n"
    }
}