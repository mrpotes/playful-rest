package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait ReplaceAll[T] extends RequestHandler {
  
  def replaceAll(objs: TraversableOnce[T]): Unit

}