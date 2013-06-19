package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait Create[T] extends RequestHandler {

  def create(obj: T): Option[String]
  
}