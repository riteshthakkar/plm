package actors

import akka.actor.Actor
import play.api._
import collection.JavaConversions._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.api.Play.current
import models._
import microsoft.exchange.webservices.data.Folder
import microsoft.exchange.webservices.data.Contact


class ContactSyncActor extends Actor {
  
	  val service = new ExchangeService();
	  
	  def receive = {
	    /** to start the synchronization process **/
	    case u: Account =>
	      
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      
	      // start syncing contact folder
	      // create folder in the system
	      val contacts = Folder.bind(service, WellKnownFolderName.Contacts);
	      val iFolder = new models.Folder(u.id, contacts.getDisplayName(), "contacts", contacts.getId().getUniqueId())
	      
	      val view = new ItemView(50)
	      val results = service.findItems(contacts.getId(), view)
	      
	      val contactCnt = results.getItems().foreach {
	        i =>
	          /* for below line..
	           *  instead of EmailMessage some webservice 
	           * like  contacts equiv ....mostly this shud work*/
	          val contactDetails = Contact.bind(service, i.getId);
	          contactDetails.load
	         //val c = new models.Contact(contactDetails.get,contactDetails.getGivenName(),contactDetails.FName(), contactDetails.LName(),contactDetails.getEmailId().map{_.getAddress()}.toList,contactDetails.getDisplayName(),contactDetails.getAddress().map{_.getAddress()}.toList, contactDetails.getPhoneNos().map{_.getNos()}.toList,contactDetails.getNotes(),contactDetails.getBday(),contactDetails.getHasPicture(), contactDetails.getId().toString(), iFolder.id)
	         //models.Contact.save(c)
	      }
	  }

}