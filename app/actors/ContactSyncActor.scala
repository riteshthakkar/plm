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
	  
	  /** to start the synchronization process **/
	    def receive = {
	    
	    case u: Account =>
	      
	      val credentials = new WebCredentials(u.username, u.password, "")
	      service.setCredentials(credentials)
	      service.setUrl(new URI(u.serverURI))
	      
	      // start syncing contact folder
	      // create folder in the system
	      val contactFolder = Folder.bind(service, WellKnownFolderName.Contacts);
	      val cFolder = new models.Folder(u.id, contactFolder.getDisplayName(), "contactsRoot", contactFolder.getId().getUniqueId())
	      
	      val searchFilter = new SearchFilter.IsGreaterThan(FolderSchema.TotalCount, 0)
	      val findResult = service.findFolders(WellKnownFolderName.Contacts, searchFilter, new FolderView(50))
	     
	      
	       val folder = findResult.getFolders().foreach{
	        i =>
	          val fId = i.getId()
	          
	          val view = new ItemView(50)
	          val results = service.findItems(i.getId(), view)
	          
	          while(results.isMoreAvailable()){
	          val subjectList = results.getItems().foreach {
	        	  i =>
	          val contactDetails = Contact.bind(service, i.getId);
	          contactDetails.load
	          val c = new models.Contact(u.getId,contactDetails.getGivenName(),contactDetails.getNickName(), contactDetails.getSurname(),contactDetails.getDisplayName(),contactDetails.getEmailAddresses().getEmailAddress(EmailAddressKey.EmailAddress1).toString(),contactDetails.getEmailAddresses().getEmailAddress(EmailAddressKey.EmailAddress2).toString(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getStreet(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getCity(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getState(), contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getPostalCode(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Business).getCountryOrRegion(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Home).getStreet(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Home).getCity(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Home).getState(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Home).getPostalCode(),contactDetails.getPhysicalAddresses().getPhysicalAddress(PhysicalAddressKey.Home).getCountryOrRegion(),contactDetails.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.BusinessPhone).toInt,contactDetails.getPhoneNumbers().getPhoneNumber(PhoneNumberKey.HomePhone).toInt,contactDetails.getBirthday().toString(), contactDetails.getId().toString(), cFolder.id)
	          models.Contact.save(c)
	      }}
	      }
	     
	  }

}