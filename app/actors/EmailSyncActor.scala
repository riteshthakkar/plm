package actors

import akka.actor.Actor
import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI

import models._


class EmailSyncActor extends Actor {
  
	  val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case u: User => 
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      
	      
	      
	      // save the user object in the db.
	      //User.save(u)
	      val view = new ItemView(50)
	      val results = service.findItems(WellKnownFolderName.Inbox, view)
	      val subjectList = results.getItems().map {
	        i => i.getSubject()
          }
	      
	      subjectList.toList
	      
	  }

}