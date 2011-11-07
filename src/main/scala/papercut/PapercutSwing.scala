//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut

import scala.swing.Action
import scala.swing.Button
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication

import playn.core.PlayN
import playn.java.JavaPlatform

import react.Slot
import react.UnitSlot

import flashbang.AppMode
import flashbang.Flashbang

object PapercutSwing extends SimpleSwingApplication {
  implicit def func0ToSlot (f :() => _) = new UnitSlot() { def onEmit () = f() }
  implicit def func1ToSlot[T] (f :(T) => _) = new Slot[T]() { def onEmit (t :T) = f(t) }

  val play = new Button(Action("Play") {
    val mode = Flashbang.app.defaultViewport.topMode.asInstanceOf[AnimateMode]
    mode.playing.update(!mode.playing.get())
  })
  def top = new MainFrame {
    title = "Papercut"
    contents = play
  }

  override def startup (args :Array[String]) {
    val prefix = "src/main/resources"
    JavaPlatform.register().assetManager().setPathPrefix(prefix);
    PlayN.run(new PapercutApp(new LocalAssets(prefix)))
    // Once AnimateMode is in place, hook the play button's text to the mode's state
    Flashbang.app.defaultViewport.topModeChanged.connect((mode :AppMode) => mode match {
      case anim :AnimateMode => anim.playing.connect(() =>
        play.text = if (anim.playing.get) "Stop" else "Play"
      )
    })
    super.startup(args)
  }
}
