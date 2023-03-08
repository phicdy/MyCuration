package com.phicdy.mycuration.domain.rss

class Atom {
    fun text() = """
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" media="screen" href="/~d/styles/atom10full.xsl"?><?xml-stylesheet type="text/css" media="screen" href="https://feeds.feedburner.com/~d/styles/itemcontent.css"?><feed xmlns="https://www.w3.org/2005/Atom" xmlns:feedburner="https://rssnamespace.org/feedburner/ext/1.0">
    <id>https://androiddagashi.github.io/</id>
    <title>Android Dagashi</title>
    <updated>2019-03-03T10:53:35.000Z</updated>
    <generator>https://github.com/jpmonette/feed</generator>
    <author>
        <name>Android Dagashi</name>
        <uri>https://androiddagashi.github.io/</uri>
    </author>
    <link rel="alternate" href="https://androiddagashi.github.io/" />
    <subtitle>Weekly Android developer news digest in Japanese</subtitle>
    <icon>https://androiddagashi.github.io/favicon.ico</icon>
    <rights>All rights reserved 2019, Android Dagashi</rights>
    <category term="Android">
    </category>
    <contributor>
        <name>@_yshrsmz</name>
        <uri>https://twitter.com/_yshrsmz</uri>
    </contributor>
    <contributor>
        <name>@hydrakecat</name>
        <uri>https://twitter.com/hydrakecat</uri>
    </contributor>
    <atom10:link xmlns:atom10="https://www.w3.org/2005/Atom" rel="self" type="application/atom+xml" href="https://feeds.feedburner.com/AndroidDagashi" /><feedburner:info uri="androiddagashi" /><atom10:link xmlns:atom10="https://www.w3.org/2005/Atom" rel="hub" href="https://pubsubhubbub.appspot.com/" /><entry>
        <title type="html"><![CDATA[#57 2019-03-03]]></title>
        <id>https://androiddagashi.github.io/issue/57-2019-03-03</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/saI5mOCH5sg/57-2019-03-03">
        </link>
        <updated>2019-03-03T10:53:35.000Z</updated>
        <summary type="html">&lt;p&gt;Flutter 1.2、Android StudioチームのAMA、AndroidXのCIビルドが見れるページが公開、マルチモジュールについてのアンケート結果、など。&lt;/p&gt;
Android×MVI×Coroutineなアーキテクチャを提供するライブラリ、Owl/Material Components 1.1.0-alpha04/マルチモジュールについてのアンケート結果/5KB以下のDartコードでFlutterアプリを作るコンテスト、Flutter Create/AndroidXのCIビルドが見れるページが公開/FBの画像処理ライブラリSpectrum 1.0.0 が公開/Android StudioチームのAMA/Flutter 1.2/ダークモードを実装するには/Fragment APIへの提言/Kotlinx Serializationを使ったJAX-RSのReader/Writer&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/saI5mOCH5sg" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/57-2019-03-03</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#56 2019-02-24]]></title>
        <id>https://androiddagashi.github.io/issue/56-2019-02-24</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/XBDxgb_-TrI/56-2019-02-24">
        </link>
        <updated>2019-02-24T10:36:26.000Z</updated>
        <summary type="html">&lt;p&gt;アプリの名前を変えたらストアから削除された話、GoogleによるAndroid開発者向けサーベイ、ViewPager2の仕組み、Apply Changesの裏側、など&lt;/p&gt;
Kotlin DSL を考慮した Gradle Plugin を記述するために必要だったこと/ViewPager2の仕組み/Kotlinがいかにしてメモリリークを避ける助けになるか/Androidアプリのモジュール化について/AAC Navigation 1.0.0-rc1リリース/Apply Changesの裏側/2019年秋からPlay Storeでの公開にはtargetSdkVersion 28が必須/GoogleによるAndroid開発者向けサーベイ/KotlinConf 2019のCall for Speakersが開始/メッセージングアプリのSignalがPlay Storeのアップデートでトラブル/Android Qで戻るボタンが消えるという噂/アプリの名前を変えたらストアから削除された話&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/XBDxgb_-TrI" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/56-2019-02-24</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#55 2019-02-17]]></title>
        <id>https://androiddagashi.github.io/issue/55-2019-02-17</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/dX6p3G8xyyg/55-2019-02-17">
        </link>
        <updated>2019-02-17T10:49:45.000Z</updated>
        <summary type="html">&lt;p&gt;DroidKaigiのセッション動画が公開、Kotlin/Nativeのスレッドについて、AndroidThingsのサポートデバイスが縮小、Robolectric 4.2、WorkManager1.0.0-rc1、など&lt;/p&gt;
DroidKaigi 2019のセッションまとめ/WorkManager 1.0.0-rc1リリース/マルチモジュールプロジェクトでの Dagger2を用いた Dependency Injection/OSの分布ダッシュボードが2018年10月から更新されていない件/Robolectric 4.2 リリース/RetrofitにKotlin CoroutinesサポートのPRがマージされる/Kotlin/Nativeのスレッドについて/Kotlinスコープ関数を使う時/Play Storeのアプリ取り締まりの方針は今後も厳しいまま/Android Thingsのサポートデバイスが縮小/DroidKaigi 2019のセッション動画が公開/中間モジュールを挟んでビルド時間を短縮するテクニック/windowIsTranslucentがtrueでportraitのアクティビティはやめた方がいい&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/dX6p3G8xyyg" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/55-2019-02-17</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#54 2019-02-10]]></title>
        <id>https://androiddagashi.github.io/issue/54-2019-02-10</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/ner_FeTrR_g/54-2019-02-10">
        </link>
        <updated>2019-02-10T10:34:08.000Z</updated>
        <summary type="html">&lt;p&gt;DroidKaigi2019資料まとめ、ViewPager2、AndroidStudio 3.3.1、OkHttpのminSdkVersionが21に、KotlinConf2019、など&lt;/p&gt;
DroidKaigi2019資料まとめ/ViewPager2 1.0.0-alpha01/Android Studio 3.3.1/Room 2.1のKotlin Coroutinesサポート/誤ってnon-finalなclassが公開されるのを防ぐライブラリ/Material-Component 1.1.0-alpha03リリース/OkHttp 3.13のminSdkVersionが21に/iOSにあってAndroidにない欲しい機能/Kotlin Conf 2019の日付と場所が発表/Navigation 1.0.0-beta01/WorkManager 1.0.0-beta04&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/ner_FeTrR_g" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/54-2019-02-10</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#53 2019-02-03]]></title>
        <id>https://androiddagashi.github.io/issue/53-2019-02-03</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/t9VB4CXQqO4/53-2019-02-03">
        </link>
        <updated>2019-02-03T10:11:07.000Z</updated>
        <summary type="html">&lt;p&gt;DroidKaigi2019のアプリがリリース、サンタトラッカーのソースコードが公開、リフレクションベースのDagger2、など&lt;/p&gt;
Fuchsiaは未来なのか？/DroidKaigi2019のアプリがリリース/Flutterのアーキテクチャ入門/PWAをPlay Storeに公開する準備（？）が整った/リフレクションベースのDagger2/2019/01/30のAndroidxリリース/実機でのデザイン確認をより便利にするアプリWindow/なぜkotlinx.android.syntheticはAOSPで推奨されないのか/サンタトラッカーのソースコードが公開/GradleのincludeGroupでライブラリを取得するレポジトリを指定する/1つのFragmentで複数のViewModelを持つのは悪いのか&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/t9VB4CXQqO4" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/53-2019-02-03</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#52 2019-01-27]]></title>
        <id>https://androiddagashi.github.io/issue/52-2019-01-27</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/7zEosfL_BE8/52-2019-01-27">
        </link>
        <updated>2019-01-27T10:50:52.000Z</updated>
        <summary type="html">&lt;p&gt;Google I/Oの日程が明らかに、Android NDK 19、Kotlin 1.3.20、天下一「AndroidのORM」武道会、BitriseがFlutterをサポート、WorkManager 1.0.0-beta03、など&lt;/p&gt;
Android Qで顔認証が正式サポートされる？/エミュレータ28.1.4 Canary/WorkManager 1.0.0-beta03/Google I/O 2019の日程が明らかになる/BitriseのFlutter CIがv1.0に到達/Google v. Oracle最高裁へ/AAC Navigation 1.0.0-alpha10とalpha11がリリース/Kotlin 1.3.20リリース/KotlinがGoogle内で公式のAndroid開発用言語となる/Android NDK 19/天下一「AndroidのORM」武道会（2019: FINAL）&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/7zEosfL_BE8" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/52-2019-01-27</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#51 2019-01-20]]></title>
        <id>https://androiddagashi.github.io/issue/51-2019-01-20</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/SzclOaGeKlo/51-2019-01-20">
        </link>
        <updated>2019-01-20T10:12:14.000Z</updated>
        <summary type="html">&lt;p&gt;Android Studio 3.3、複数モジュール環境でのDI、Instant Runの後継機能Apply Changes、8月から64bitサポートが必須に、など。&lt;/p&gt;
Google Play App Translation Service/年収を共有するスレッド/高セキュリティなアプリを作るためにどんなことを調べればいい？/2019年8月から64bitサポートが必須に/JetBrains ToolboxでAndroid Studioが管理可能に/複数モジュール環境でのDI/Kotlin 1.3.20 EAP 3が利用可能に/Android Studio 3.5 Alpha 1の新機能、Apply Changes/SMS/Callパーミッションのポリシーに適合しないアプリ削除が始まる/Android Studio 3.3の安定板がリリース/バックグラウンドのアプリをkillするベンダーリスト&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/SzclOaGeKlo" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/51-2019-01-20</feedburner:origLink></entry>
    <entry>
        <title type="html"><![CDATA[#50 2019-01-13]]></title>
        <id>https://androiddagashi.github.io/issue/50-2019-01-13</id>
        <link href="https://feedproxy.google.com/~r/AndroidDagashi/~3/54Del-tnIR0/50-2019-01-13">
        </link>
        <updated>2019-01-13T10:51:36.000Z</updated>
        <summary type="html">&lt;p&gt;DroidKaigi 2019のタイムテーブルが公開される、DroidKaigi 2019公式アプリのコードが公開される、SQLDelight 1.0.0リリース、Looperの閉じ忘れでファイルディスクリプタが枯渇する話など。&lt;/p&gt;
DBのIDをinline classで表現する/Androidのコードはハンガリアン記法をやめたの？/DroidKaigi 2019公式アプリのコードが公開される/DroidKaigi 2019のタイムテーブルが公開される/Looperの閉じ忘れでファイルディスクリプタが枯渇する話/SQLDelight 1.0.0リリース/技術記事を検索してもチュートリアルばかり出てくるという嘆き/複数Activityにまたがるデータをどう管理するか/Android Qがシステム全体でダークモードを持つという噂/クライアントとサーバーどちらに実装するかの設計指針をチームで持つこと&lt;img src="https://feeds.feedburner.com/~r/AndroidDagashi/~4/54Del-tnIR0" height="1" width="1" alt=""/&gt;</summary>
    <feedburner:origLink>https://androiddagashi.github.io/issue/50-2019-01-13</feedburner:origLink></entry>
</feed>
    """.trimIndent()
}