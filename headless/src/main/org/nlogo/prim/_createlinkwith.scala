// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentSet }
import org.nlogo.api.{ Syntax, I18N, AgentKind }
import org.nlogo.nvm.{ Command, Context, EngineException,
                       CustomAssembled, AssemblerAssistant }

class _createlinkwith(val breedName: String) extends Command with CustomAssembled {

  def this() = this("")

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.AgentType, Syntax.CommandBlockType | Syntax.OptionalType),
      "-T--", "---L", true)

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context) {
    var dest = argEvalTurtle(context, 0)
    var src = context.agent.asInstanceOf[Turtle]
    val breed =
      if (breedName.isEmpty)
        world.links
      else
        world.getLinkBreed(breedName)
    mustNotBeDirected(breed, context)
    checkForBreedCompatibility(breed, context)
    if (breed eq world.links)
      breed.setDirected(false)
    if (world.linkManager.findLinkEitherWay(src, dest, breed, false) == null) {
      if (src eq dest)
        throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.turtleCantLinkToSelf"))
      if (src.id > dest.id) {
        val tmp = src
        src = dest;
        dest = tmp
      }
      if (src.id != -1 && dest.id != -1) {
        val link = world.linkManager.createLink(src, dest, breed)
        workspace.joinForeverButtons(link)
        if (offset - context.ip > 2)
          context.runExclusiveJob(AgentSet.fromAgent(link), next)
      }
    }
    context.ip = offset
  }

  def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
