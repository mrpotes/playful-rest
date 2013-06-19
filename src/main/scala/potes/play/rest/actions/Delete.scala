package potes.play.rest.actions

import potes.play.rest.RequestHandler

trait Delete extends RequestHandler {
  
  def delete(id: String): Boolean

}