package actors

import akka.actor.Actor
import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.api.Play.current
import models._
import microsoft.exchange.webservices.data.Folder

class EmailSyncActor extends Actor {
  
	  val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case u: Account =>
	      
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      //service.
	      
	      // start syncing inbox, outbox and deleted items folder
	      // create folder in the system
	      //
	      val rootFolder = Folder.bind(service, WellKnownFolderName.Root)
	      val rFolder = new models.Folder(u.id, rootFolder.getDisplayName(), "root", rootFolder.getId().getUniqueId())
	      
	      //val view = new ItemView(50)
	      //val results = service.findItems(rootFolder.getId(), view)   
	      
	      val searchFilter = new SearchFilter.IsGreaterThan(FolderSchema.TotalCount, 0)
	      val findResult = service.findFolders(WellKnownFolderName.MsgFolderRoot, searchFilter, new FolderView(50))
	     
	      
	      
	      val folder = findResult.getFolders().foreach{
	        i =>
	          val fId = i.getId()
	          
	          val view = new ItemView(50)
	          val results = service.findItems(i.getId(), view)
	          while(results.isMoreAvailable()){
	          val subjectList = results.getItems().foreach {
	        	  i =>
	          val message = EmailMessage.bind(service, i.getId);
	          message.load
	          val e = new Email(u.id, message.getFrom().getAddress(), message.getToRecipients().map{_.getAddress()}.toList, message.getCcRecipients().map{_.getAddress()}.toList, message.getBccRecipients().map{_.getAddress()}.toList, message.getSubject(), message.getBody().toString(), message.getId().toString(), message.getDateTimeReceived().getTime(),fId.toString())
	          Email.save(e)
	      }}
	      }
	          //val subscription = service.subscribeToPullNotifications(i.getId.toString(),5, null, EventType.NewMail, EventType.Created, EventType.Deleted)
	      
	       
	  }

}