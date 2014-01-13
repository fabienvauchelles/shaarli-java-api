<h1>shaarli-java-api</h1>

shaarli-java-api is a java client api for Sebsauvage's <a href="http://sebsauvage.net/wiki/doku.php?id=php:shaarli">Shaarli</a>.

You can find a tutorial <a href="http://fabien.vauchelles.com/shaarli-java-api">here</a> (in french).

<h2>Why use shaarli ?</h2>

It's <a href="http://en.wikipedia.org/wiki/KISS_principle">KISS</a>.

<h2>Why use shaarli-java-api ?</h2>

Just a few reasons :
<ul>
<li>it's <b><a href="http://en.wikipedia.org/wiki/KISS_principle">KISS</a></b> (again, it's boring?) ;</li>
<li>it's <b>fast</b> (based on Apache Http Client) ;</li>
<li>it's <b>easy</b> to deploy with Maven ;</li>
<li>it's <b>safe</b> (with unit tests, a code control with <a href="http://checkstyle.sourceforge.net/">Checkstyle</a>, <a href="http://pmd.sourceforge.net/">PMD</a> and <a href="http://findbugs.sourceforge.net/">Findbugs</a>) ;</li>
</ul>

<h2>Which features can i use ?</h2>

You can :
<ul>
<li>Iterate all links (and reverse) ;</li>
<li>Iterate links by multiple tags (and reverse) ;</li>
<li>Iterate links by a term (and reverse) ;</li>
<li>Count links ;</li>
<li>Add a new post ;</li>
<li>Modify an existing post ;</li>
<li>Delete an existing post ;</li>
<li>Define your own hooks to use a customized Shaarli (like <a href="http://lesliensducode.com">lesliensducode.com</a>).</li>
</ul>

<h2>How to use it ?</h2>

<ol>
<li>Add this to your pom.xml file :

<pre>
&lt;dependency&gt;
  &lt;groupId&gt;com.vaushell.shaarli-java-api&lt;/groupId&gt;
  &lt;artifactId&gt;shaarli-java-api&lt;/artifactId&gt;
  &lt;version&gt;REPLACE_WITH_LAST_VERSION&lt;/version&gt;
&lt;/dependency&gt;
</pre>
</li>

<li>See this tutorial <a href="http://fabien.vauchelles.com/shaarli-java-api">here</a> (in french) ;</li>
<li>See example file <a href="https://github.com/fabienvauchelles/shaarli-java-api/blob/master/shaarli-java-api/src/test/java/com/vaushell/shaarlijavaapi/Examples.java">here</a> ;</li>
<li>See  unit tests <a href="https://github.com/fabienvauchelles/shaarli-java-api/blob/master/shaarli-java-api/src/test/java/com/vaushell/shaarlijavaapi/ShaarliClientTest.java">here</a> ;</li>
<li>Do it in your code !</li>
</ol>

<h2>Licence</h2>

See <a href="https://github.com/fabienvauchelles/shaarli-java-api/blob/master/LICENCE.txt">LICENCE.txt</a>.

