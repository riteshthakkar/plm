package actors

import akka.actor.Actor
import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.api.Play.current
import models._
import microsoft.exchange.webservices.data.Folder

case class Recent(email: String, skip: Int, limit: Int)

class ContactApiActor extends Actor {
  
	  val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case r: Recent =>
	      val account = Account.findByEmail(r.email)
	      val a = account.head
	      val emails = Email.findByUser(a.id, r.skip, r.limit)
	      sender ! emails
	      
	  }

}