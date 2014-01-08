# shaarli-java-api

shaarli-java-api is a java client api for Sebsauvage's [Shaarli](http://sebsauvage.net/wiki/doku.php?id=php:shaarli).

You can find a tutorial [here](http://fabien.vauchelles.com/shaarli-java-api) (in french).

## Why use shaarli ?

It's [KISS](http://en.wikipedia.org/wiki/KISS_principle).

## Why use shaarli-java-api ?

Just a few reasons :
<ul>
<li>it's <b>[KISS](http://en.wikipedia.org/wiki/KISS_principle)</b> (again, it's boring?) ;</li>
<li>it's <b>fast</b> (based on Apache Http Client) ;</li>
<li>it's <b>easy</b> to deploy with Maven ;</li>
<li>it's <b>safe</b> (with unit tests, a code control with <a href="http://checkstyle.sourceforge.net/">Checkstyle</a>, <a href="http://pmd.sourceforge.net/">PMD</a> and <a href="http://findbugs.sourceforge.net/">Findbugs</a>) ;</li>
</ul>

## Which features can i use ?

You can :
<ul>
<li>Iterate all links ;</li>
<li>Iterate links by multiple tags ;</li>
<li>Iterate links by a term ;</li>
<li>Count links ;</li>
<li>Add a new post ;</li>
<li>Modify an existing post ;</li>
<li>Delete an existing post ;</li>
<li>Define your own hooks to use a template.</li>
</ul>

## How to use it ?

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

## Licence

See [LICENCE.txt](https://github.com/fabienvauchelles/shaarli-java-api/blob/master/LICENCE.txt).

