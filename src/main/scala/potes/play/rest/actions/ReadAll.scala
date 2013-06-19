package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait ReadAll[T] extends RequestHandler {
  
  def readAll: List[T]

}