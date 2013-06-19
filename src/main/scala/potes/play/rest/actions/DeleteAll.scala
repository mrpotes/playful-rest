package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait DeleteAll extends RequestHandler {
  
  def deleteAll: Unit

}