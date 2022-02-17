package com.phicdy.mycuration.domain.rss

class FeedBurnerAndroidDeveloperBlog {
    val text = """<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" media="screen" href="/~d/styles/atom10full.xsl"?>
<?xml-stylesheet type="text/css" media="screen" href="https://feeds.feedburner.com/~d/styles/itemcontent.css"?>
<feed xmlns="https://www.w3.org/2005/Atom"
    xmlns:openSearch="https://a9.com/-/spec/opensearchrss/1.0/"
    xmlns:blogger="https://schemas.google.com/blogger/2008"
    xmlns:georss="https://www.georss.org/georss"
    xmlns:gd="https://schemas.google.com/g/2005"
    xmlns:thr="https://purl.org/syndication/thread/1.0">
    <id>tag:blogger.com,1999:blog-6755709643044947179</id>
    <updated>2022-02-16T13:24:22.291-08:00</updated>
    <category term="Featured" />
    <category term="Android" />
    <category term="wear emulators" />
    <category term="wear-tiles" />
    <category term="web registry" />
    <category term="wellness" />
    <title type="text">Android Developers Blog</title>
    <subtitle type="html">An Open Handset Alliance Project.</subtitle>
    <link rel="alternate" type="text/html" href="https://android-developers.googleblog.com/" />
    <link rel="next" type="application/atom+xml" href="https://www.blogger.com/feeds/6755709643044947179/posts/default?start-index=26&amp;max-results=25&amp;redirect=false" />
    <author>
        <name>Ian Lake</name>
        <uri>https://www.blogger.com/profile/17415160793077313560</uri>
        <email>noreply@blogger.com</email>
        <gd:image rel="https://schemas.google.com/g/2005#thumbnail" width="16" height="16" src="https://img1.blogblog.com/img/b16-rounded.gif" />
    </author>
    <generator version="7.00" uri="https://www.blogger.com">Blogger</generator>
    <openSearch:totalResults>1319</openSearch:totalResults>
    <openSearch:startIndex>1</openSearch:startIndex>
    <openSearch:itemsPerPage>25</openSearch:itemsPerPage>
    <atom10:link xmlns:atom10="https://www.w3.org/2005/Atom" rel="self" type="application/atom+xml" href="https://feeds.feedburner.com/blogspot/hsDu" />
    <feedburner:info xmlns:feedburner="https://rssnamespace.org/feedburner/ext/1.0" uri="blogspot/hsdu" />
    <atom10:link xmlns:atom10="https://www.w3.org/2005/Atom" rel="hub" href="https://pubsubhubbub.appspot.com/" />
    <entry>
        <id>tag:blogger.com,1999:blog-6755709643044947179.post-3155562637345734734</id>
        <published>2022-02-11T10:05:00.003-08:00</published>
        <updated>2022-02-15T08:16:27.664-08:00</updated>
        <category scheme="https://www.blogger.com/atom/ns#" term="Android" />
        <category scheme="https://www.blogger.com/atom/ns#" term="documentation" />
        <category scheme="https://www.blogger.com/atom/ns#" term="Featured" />
        <category scheme="https://www.blogger.com/atom/ns#" term="latest" />
        <category scheme="https://www.blogger.com/atom/ns#" term="Testing" />
        <category scheme="https://www.blogger.com/atom/ns#" term="Tools" />
        <title type="text">Write better tests with the new testing guidance</title>
        <content type="html">&lt;meta name="twitter:image" content="https://blogger.googleusercontent.com/img/a/AVvXsEijqPilH8ZItDxsIE5xhn5FozKBwNZGPnfzcvdJmumZp0fI8UmN0aKMqCHNHjopTW3uqUlroMP3IFV9LrUoQ2aqXbgpDaqkrjqoQJEuBE6Smt88Q5nz-NgAlGWXjq8eXz0ERxVtfrrx7v6MhNoL1OV43oCVgHPSfmrSu4_YWAIUKzwz8k59ErcCplII"&gt;
&lt;img style="display:none" src="https://blogger.googleusercontent.com/img/a/AVvXsEijqPilH8ZItDxsIE5xhn5FozKBwNZGPnfzcvdJmumZp0fI8UmN0aKMqCHNHjopTW3uqUlroMP3IFV9LrUoQ2aqXbgpDaqkrjqoQJEuBE6Smt88Q5nz-NgAlGWXjq8eXz0ERxVtfrrx7v6MhNoL1OV43oCVgHPSfmrSu4_YWAIUKzwz8k59ErcCplII"&gt;

&lt;p&gt;&lt;em&gt;Posted by &lt;a href="https://twitter.com/ppvi"&gt;Jose Alcérreca&lt;/a&gt;, Android Developer Relations Engineer&lt;/em&gt;&lt;p&gt;


&lt;center&gt; &lt;a href="https://blogger.googleusercontent.com/img/a/AVvXsEijqPilH8ZItDxsIE5xhn5FozKBwNZGPnfzcvdJmumZp0fI8UmN0aKMqCHNHjopTW3uqUlroMP3IFV9LrUoQ2aqXbgpDaqkrjqoQJEuBE6Smt88Q5nz-NgAlGWXjq8eXz0ERxVtfrrx7v6MhNoL1OV43oCVgHPSfmrSu4_YWAIUKzwz8k59ErcCplII" imageanchor="1" &gt;&lt;img style="width:100%" alt="Blue illustration with Android phone" id=imgFull border="0" src=" https://blogger.googleusercontent.com/img/a/AVvXsEijqPilH8ZItDxsIE5xhn5FozKBwNZGPnfzcvdJmumZp0fI8UmN0aKMqCHNHjopTW3uqUlroMP3IFV9LrUoQ2aqXbgpDaqkrjqoQJEuBE6Smt88Q5nz-NgAlGWXjq8eXz0ERxVtfrrx7v6MhNoL1OV43oCVgHPSfmrSu4_YWAIUKzwz8k59ErcCplII" data-original-width="1058" data-original-height="714" /&gt;&lt;/a&gt; &lt;/center&gt;
  
  
&lt;p&gt;
As apps increase in functionality and complexity, manually testing them to verify behavior becomes tedious, expensive, or impossible. Modern apps, even simple ones, require you to verify an ever-growing list of test points such as UI flows, localization, or database migrations. Having a QA team whose job is to manually verify that the app works is an option, but fixing bugs at that stage is expensive. The sooner you fix a problem in the development process the better.
&lt;/p&gt;
&lt;p&gt;
Automating tests is the best approach to catching bugs early. Automated testing (from now on, &lt;em&gt;testing&lt;/em&gt;) is a broad domain and Android offers many tools and libraries that can overlap. For this reason, beginners often find testing challenging.
&lt;/p&gt;
&lt;p&gt;
In response to this feedback, and to accommodate for Compose and new architecture guidelines, we revamped two testing sections on &lt;a href="https://d.android.com"&gt;d.android.com&lt;/a&gt;:
&lt;/p&gt;
&lt;h2&gt;Training&lt;/h2&gt;


&lt;p&gt;
Firstly, there is the new &lt;strong&gt;&lt;a href="https://developer.android.com/training/testing"&gt;Testing training&lt;/a&gt;&lt;/strong&gt;, which includes the fundamentals of testing in Android with two new articles: &lt;a href="https://developer.android.com/training/testing/fundamentals/what-to-test"&gt;What to test&lt;/a&gt;, an opinionated guide for beginners, and a detailed guide on &lt;a href="https://developer.android.com/training/testing/fundamentals/test-doubles"&gt;Test doubles&lt;/a&gt;. 
&lt;/p&gt;

&lt;div class="separator" style="clear: both;"&gt;&lt;a href="https://blogger.googleusercontent.com/img/a/AVvXsEiWOemUDpatCmMa1JiwWWP3NCc3w5SIFV1-IocrL8kWb2II0YCa40Qy2hECirxpg76Q3fPeZ3WNycAK-KZJsefj6Sp77hvo_Rv4rU4s5nlVc-lAAAOg5YZxlcVbvr9Az512gMZKNPf2cfEyfyySRNDPGDI4N0d49Z5AXt5ivVRMrA3KvnomY7CORwZn" style="display: block; padding: 1em 0; text-align: center; "&gt;&lt;img alt="Faking dependencies in unit tests" style="width:50%" border="0" data-original-height="1270" data-original-width="1296" src="https://blogger.googleusercontent.com/img/a/AVvXsEiWOemUDpatCmMa1JiwWWP3NCc3w5SIFV1-IocrL8kWb2II0YCa40Qy2hECirxpg76Q3fPeZ3WNycAK-KZJsefj6Sp77hvo_Rv4rU4s5nlVc-lAAAOg5YZxlcVbvr9Az512gMZKNPf2cfEyfyySRNDPGDI4N0d49Z5AXt5ivVRMrA3KvnomY7CORwZn"/&gt;&lt;/a&gt;&lt;/div&gt;&lt;p id="imgCaption"&gt;Faking dependencies in unit tests&lt;/p&gt;

&lt;p&gt;
&lt;br&gt;After providing an overview of the theory, the guide focuses on practical examples of the two main types of tests. 
&lt;/p&gt;
&lt;ul&gt;

&lt;li&gt;&lt;a href="https://developer.android.com/training/testing/local-tests"&gt;Local tests&lt;/a&gt; that run on a workstation and are typically unit tests.

&lt;li&gt;&lt;a href="https://developer.android.com/training/testing/instrumented-tests"&gt;Instrumented tests&lt;/a&gt; that run on a device. This section includes an introduction to &lt;a href="https://developer.android.com/training/testing/instrumented-tests/ui-tests"&gt;UI tests&lt;/a&gt; and the &lt;a href="https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/test-setup"&gt;AndroidX Test libraries&lt;/a&gt;.


&lt;div class="separator" style="clear: both;"&gt;&lt;a href="https://blogger.googleusercontent.com/img/a/AVvXsEjZSF5fsPjJqTU_3FnK1MlgAswaVi_abQ4w9kb9qRs4Z3QmKDiT2VTdf4lNspufU6E3RlzkfWMxgBkQgPTlVJCo_Z5o1f0PZcwxZGgkDuJlllCBFxpjmlVjRzBxBPQXOunDNj-Hy340U4Ri3tG9lWHlBKQC8DN4rI0YEdupdnVeTGCXZ-Dtt7AXpU2z" style="display: block; padding: 1em 0; text-align: center; "&gt;&lt;img style="width:50%"alt="Faking dependencies in UI tests" border="0" data-original-height="1365" data-original-width="1999" src="https://blogger.googleusercontent.com/img/a/AVvXsEjZSF5fsPjJqTU_3FnK1MlgAswaVi_abQ4w9kb9qRs4Z3QmKDiT2VTdf4lNspufU6E3RlzkfWMxgBkQgPTlVJCo_Z5o1f0PZcwxZGgkDuJlllCBFxpjmlVjRzBxBPQXOunDNj-Hy340U4Ri3tG9lWHlBKQC8DN4rI0YEdupdnVeTGCXZ-Dtt7AXpU2z"/&gt;&lt;/a&gt;&lt;/div&gt;&lt;p id="imgCaption"&gt;Faking dependencies in UI tests&lt;/p&gt;


&lt;h2&gt;Tools Documentation&lt;/h2&gt;


&lt;p&gt;
Secondly, we updated the &lt;strong&gt;&lt;a href="https://developer.android.com/studio/test"&gt;Testing section of the Tools documentation&lt;/a&gt;&lt;/strong&gt; that focuses on all the tools that help you create and run tests, from &lt;a href="https://developer.android.com/studio/test/test-in-android-studio"&gt;Android Studio&lt;/a&gt; to &lt;a href="https://developer.android.com/studio/test/command-line"&gt;testing from the command line&lt;/a&gt;.
&lt;/p&gt;

&lt;div class="separator" style="clear: both;"&gt;&lt;a href="https://blogger.googleusercontent.com/img/a/AVvXsEglY1tp9_6lYQcY7IsMZd1KpOA1TQ7R_3lQ0YoIPqvKIPsQhd2RRJygpwB8RAl6b52CxqwV5YFcoCEPatnKkJWcuPJBFZgqRCJM8rKhEr_naqs7pX93MBfN6jIZO_w62APq-KSGK64S30KjAwYk1keASt0TgsZ65-Bm0oq-NjBcoz3DXInGboaMnPyV" style="display: block; padding: 1em 0; text-align: center; "&gt;&lt;img style="width:75%" alt="The Unified Gradle test runner." border="0" data-original-height="958" data-original-width="1999" src="https://blogger.googleusercontent.com/img/a/AVvXsEglY1tp9_6lYQcY7IsMZd1KpOA1TQ7R_3lQ0YoIPqvKIPsQhd2RRJygpwB8RAl6b52CxqwV5YFcoCEPatnKkJWcuPJBFZgqRCJM8rKhEr_naqs7pX93MBfN6jIZO_w62APq-KSGK64S30KjAwYk1keASt0TgsZ65-Bm0oq-NjBcoz3DXInGboaMnPyV"/&gt;&lt;/a&gt;&lt;p id="imgCaption"&gt;The Unified Gradle test runner.&lt;/p&gt;&lt;/div&gt;


&lt;p&gt;
We included an article that describes &lt;a href="https://developer.android.com/studio/test/advanced-test-setup"&gt;Advanced test setup&lt;/a&gt; features such as working with different variants, the instrumentation manifest options, or the Android Gradle Plugin settings.
&lt;/p&gt;
&lt;p&gt;
These two new sections should give you a general notion of how and where to test your Android app. To learn more about testing specific features and libraries, you should check out their respective documentation pages. For example: &lt;a href="https://developer.android.com/kotlin/flow/test"&gt;Testing Kotlin flows&lt;/a&gt;, &lt;a href="https://developer.android.com/guide/navigation/navigation-testing"&gt;Test Navigation&lt;/a&gt;, or the &lt;a href="https://developer.android.com/training/dependency-injection/hilt-testing"&gt;Hilt testing guide&lt;/a&gt;.
&lt;/p&gt;
&lt;p&gt;
Sadly, machines can't automatically verify the correctness of our documentation, so if you find errors or have suggestions, please file a bug on our &lt;a href="https://issuetracker.google.com/issues/new?component=192697&amp;template=845603&amp;pli=1"&gt;documentation issue tracker&lt;/a&gt;. 
&lt;/p&gt;&lt;div class="feedflare"&gt;
&lt;a href="https://feeds.feedburner.com/~ff/blogspot/hsDu?a=R3A-3W_MDRk:7xl18cBzn0Q:yIl2AUoC8zA"&gt;&lt;img src="https://feeds.feedburner.com/~ff/blogspot/hsDu?d=yIl2AUoC8zA" border="0"&gt;&lt;/img&gt;&lt;/a&gt; &lt;a href="https://feeds.feedburner.com/~ff/blogspot/hsDu?a=R3A-3W_MDRk:7xl18cBzn0Q:-BTjWOF_DHI"&gt;&lt;img src="https://feeds.feedburner.com/~ff/blogspot/hsDu?i=R3A-3W_MDRk:7xl18cBzn0Q:-BTjWOF_DHI" border="0"&gt;&lt;/img&gt;&lt;/a&gt;
&lt;/div&gt;</content>
        <link rel="edit" type="application/atom+xml" href="https://www.blogger.com/feeds/6755709643044947179/posts/default/3155562637345734734" />
        <link rel="self" type="application/atom+xml" href="https://www.blogger.com/feeds/6755709643044947179/posts/default/3155562637345734734" />
        <link rel="alternate" type="text/html" href="https://android-developers.googleblog.com/2022/02/write-better-tests-with-new-testing.html" title="Write better tests with the new testing guidance" />
        <author>
            <name>Android Developers</name>
            <uri>https://www.blogger.com/profile/08588467489110681140</uri>
            <email>noreply@blogger.com</email>
            <gd:image rel="https://schemas.google.com/g/2005#thumbnail" width="16" height="16" src="https://img1.blogblog.com/img/b16-rounded.gif" />
        </author>
        <media:thumbnail xmlns:media="https://search.yahoo.com/mrss/" url="https://blogger.googleusercontent.com/img/a/AVvXsEijqPilH8ZItDxsIE5xhn5FozKBwNZGPnfzcvdJmumZp0fI8UmN0aKMqCHNHjopTW3uqUlroMP3IFV9LrUoQ2aqXbgpDaqkrjqoQJEuBE6Smt88Q5nz-NgAlGWXjq8eXz0ERxVtfrrx7v6MhNoL1OV43oCVgHPSfmrSu4_YWAIUKzwz8k59ErcCplII=s72-c" height="72" width="72" />
    </entry>
</feed>
"""
}