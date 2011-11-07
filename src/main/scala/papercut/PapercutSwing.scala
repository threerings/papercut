//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut

import scala.swing.Action
import scala.swing.Button
import scala.swing.MainFrame
import scala.swing.SimpleSwingApplication

import papercut.java.JavaAssetLister
import papercut.java.JavaAssetWriter

import playn.core.PlayN
import playn.java.JavaPlatform

import react.UnitSlot

import flashbang.Flashbang

object PapercutSwing extends SimpleSwingApplication {
  implicit def func0ToSlot (f :() => _) = new UnitSlot() { def onEmit () = f() }

  val play = new Button(Action("Play") {
    val mode = Flashbang.app.defaultViewport.topMode.asInstanceOf[AnimateMode]
    mode.playing.update(!mode.playing.get())
  })
  def top = new MainFrame {
    title = "Papercut"
    contents = play
  }

  override def startup (args :Array[String]) {
    JavaPlatform.register().assetManager().setPathPrefix("src/main/resources");
    Papercut.init(new JavaAssetLister(), new JavaAssetWriter());
    PlayN.run(new PapercutApp());
    // Once AnimateMode is in place, hook the play button's text to the mode's state
    Flashbang.app.defaultViewport.topModeChanged.connect(() =>
      Flashbang.app.defaultViewport.topMode match {
        case anim :AnimateMode => anim.playing.connect(() =>
          play.text = if (anim.playing.get()) "Stop" else "Play"
        )
      }
    )
    super.startup(args)
  }
}
