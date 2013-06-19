package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait Write[T] extends RequestHandler {
  
  def write(obj: T): Boolean

}