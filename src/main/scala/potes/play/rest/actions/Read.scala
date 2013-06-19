package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait Read[T] extends RequestHandler {
  
  def read(id: String): Option[T]

}