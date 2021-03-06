// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentIterator, AgentSetBuilder }
import org.nlogo.api.{ Syntax, I18N, AgentKind }
import org.nlogo.nvm.{ Command, Context, EngineException,
                       CustomAssembled, AssemblerAssistant }

class _createlinksfrom(val breedName: String) extends Command with CustomAssembled {

  def this() = this("")

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.TurtlesetType, Syntax.CommandBlockType | Syntax.OptionalType),
      "-T--", "---L", true)

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context) {
    val agentset = argEvalAgentSet(context, 0)
    val breed =
      if (breedName.isEmpty) world.links
      else world.getLinkBreed(breedName)
    mustNotBeUndirected(breed, context)
    checkForBreedCompatibility(breed, context)
    if (breed eq world.links)
      breed.setDirected(true)
    val builder = new AgentSetBuilder(AgentKind.Link, agentset.count)
    val dest = context.agent.asInstanceOf[Turtle]
    // We have to shuffle here in order for who number assignment to be random! - ST 3/15/06
    val iter = agentset.shufflerator(context.job.random)
    while(iter.hasNext) {
      val src = iter.next().asInstanceOf[Turtle]
      if (world.linkManager.findLinkFrom(src, dest, breed, false) == null) {
        if (src eq dest)
          throw new EngineException(
            context, this, I18N.errors.get(
              "org.nlogo.prim.$common.turtleCantLinkToSelf"))
        if (src.id != -1 && dest.id != -1) {
          val link = world.linkManager.createLink(src, dest, breed)
          builder.add(link)
          workspace.joinForeverButtons(link)
        }
      }
    }
    val edgeset = builder.build()
    if (offset - context.ip > 2 && edgeset.count > 0)
      context.runExclusiveJob(edgeset, next)
    context.ip = offset
  }

  def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
