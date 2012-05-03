package actors

import akka.actor.Actor
import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.libs.Akka
import akka.actor._
import akka.util._

import models._


class ExchangeAuthActor extends Actor {
   val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case u: Account => 
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      try {
	    	  //dummy call to check for authorization
	    	  service.findItems(WellKnownFolderName.Contacts, new ItemView(1)).getTotalCount()
	    	  val contactSyncActor = Akka.system.actorOf(Props[EmailSyncActor], name = "contactSyncActor")
	    	  contactSyncActor ! u 
	    	  sender ! true
	      }
	      catch {
	        case e: HttpErrorException => 
	          Logger.error(e.getStackTrace.toString())
	          sender ! false
	      }
	      
	      
	  }
  
}