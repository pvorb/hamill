hamill
======

[Akka]-based walking of arbitrary directory trees.

Download
--------

If you are using SBT, you can add the package by adding the following lines to
your `build.sbt` file.

~~~ scala
libraryDependencies += "de.vorb" %% "hamill" % "0.2.0"
~~~

Usage Example
-------------

~~~ scala
import de.vorb.hamill._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import java.nio.file.FileSystems

import scala.concurrent.duration._
import scala.concurrent.Await

object Example extends App {
  implicit val system = ActorSystem("filewalker")
  implicit val timeout = Timeout(5 seconds)

  val root = FileSystems.getDefault().getPath("src", "")

  val tracing = new Tracing

  val future = tracing.walkFileTree(root, (path: PathContainer) => {
    path match {
      case File(f, Left(err)) =>
        println("error in file " + f)
      case File(f, Right(attrs)) =>
        println(f + ", " + attrs)
      case Directory(d, attrs) =>
        println(d + ", " + attrs)
    }
  }, timeout)

  Await.result(future, 5 minutes)
}
~~~

API
---

See the [current Scaladoc API][api].

[api]: http://pvorb.github.com/hamill/api/current/#de.vorb.hamill.package
[Akka]: http://akka.io/

License
-------

Copyright © 2013 Paul Vorbach

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the “Software”), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
