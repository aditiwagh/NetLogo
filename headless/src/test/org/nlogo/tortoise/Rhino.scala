// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import
  org.nlogo.api,
  org.nlogo.util.Utils.getResourceAsString
import java.io.{ PrintWriter, StringWriter }
import sun.org.mozilla.javascript.internal.NativeArray

// There are two main entry points here: run() and eval().  The former runs compiled commands and
// collects all the lines of output.  The latter runs a compiled reporter and returns a single
// result value.

object Rhino {

  // at some point we'll need to have separate instances instead of a singleton
  val engine =
    (new javax.script.ScriptEngineManager)
      .getEngineByName("JavaScript")
  // the original CoffeeScript for this is in headless/src/main/coffee/engine.coffee.
  // sbt compiles it to JavaScript for us
  engine.eval(
    getResourceAsString("/js/engine.js"))

  // returns anything that got output-printed along the way
  def run(script: String): String = {
    val sw = new StringWriter
    engine.getContext.setWriter(new PrintWriter(sw))
    engine.eval(s"(function () {\n $script \n }).call(this);")
    sw.toString
  }

  def eval(script: String): AnyRef =
    fromRhino(engine.eval(script))

  // translate from Rhino values to NetLogo values
  def fromRhino(x: AnyRef): AnyRef =
    x match {
      case a: NativeArray =>
        api.LogoList.fromIterator(
          Iterator.from(0)
            .map(x => fromRhino(a.get(x, a)))
            .take(a.getLength.toInt))
      // this should probably reject unknown types instead of passing them through.
      // known types: java.lang.Double, java.lang.Boolean, String
      case x =>
        x
    }

}