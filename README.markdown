Ginger
======

_"Manking Java sexy"_

**Warning, this lib is not even in "alpha" version. It's still under development.**


Rationale or "Why this now?"
----------------------------

Java is a nice language, but it have some inconvenient parts. When you work 8hrs per day with Java some of those parts start to be a pain in the @$$. So, I tried to scratch my own itch.

Ginger follow some principles or "guidelines" (by order of importance):

1.  [Low Surface To Volume Ratio](http://www.laputan.org/selfish/selfish.html#LowSurfaceToVolumeRatio)
2.  [Gentle Learning Curve](http://www.laputan.org/selfish/selfish.html#GentleLearningCurve)
3.  [Programming By Difference](http://www.laputan.org/selfish/selfish.html#ProgrammingByDifference)
4.  Method Chaining (not really a fluent interface)
5.  Generic in, specific out (talking about arguments and return types)

There are 3 things in Java I don't particularly like: Reflection, Collections and Regex.

1 - Reflection
--------------

OhMy...! I just want to call a method dynamically...
Why so many objects floating around?
Why so much trouble to find my method?

The Ginger answer is an Object called "DuckType"
    
    MyReturnType result = new DuckType(myObject).call("myMethodName", arg1, arg2, ..., argX);
        
DuckType do its best to find and execute your method.

2 - Collections
---------------

Dozens of collections and none really simple to work.

I have to agree that's very difficult to make something really useful with collections when you do not have closures. Any try to implement it using anonymous classes make the code cluttered and ten times worse than using a "for" loop.

Ginger answer the problem with and Object called "Seq" that is compatible with List and Deque.

Unfortunately, I'm still working on it :)

3 - Regular Expression  
--------------

Regular expressions on Java are really cool! However, to deal with "Pattern" and "Matcher" every time you need to extract a single group is a bit too much.

Since we cannot override String (why they do care so much about me messing MY project?), we have to rely on some helper objects.

Ginger answer to it is an object called (surprise! surprise!) "Regex".

Extract something from a string can now be direct as:

    String lastWord = r("That's my name").find("\\w+$");

Or, we can get use the capture groups:

    List<String> myWords = r("<em>mark</em> <strong>them</strong>").findAll(">(\\w+)<")