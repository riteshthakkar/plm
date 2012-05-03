package controllers.api

import play.api._
import play.api.mvc._
import play.api.libs.json.Json._
import play.libs.Akka
import akka.actor._
import akka.util._
import akka.dispatch._
import akka.dispatch.Await.CanAwait
import akka.util.duration._
import play.api.libs.concurrent._
import akka.pattern.ask
import actors._
import models._
import microsoft.exchange.webservices.data._
import java.net.URI
import play.modules.mongodb.jackson.MongoDB
import models._


object ContactApiController extends Controller {
	
	implicit val timeout1 = Timeout(10 seconds) // needed for `?` below
	
	def setup = Action(parse.json) {
		implicit r => 
		  	val email = (r.body \ "email").asOpt[String].getOrElse("")
		  	val username = (r.body \ "username").asOpt[String].getOrElse("")
		  	val password = (r.body \ "password").asOpt[String].getOrElse("")
		  	val server = (r.body \ "server").asOpt[String].getOrElse("")
		  	
		  	if(email.isEmpty || username.isEmpty || password.isEmpty || server.isEmpty)
		  		Ok(toJson(Map("status" -> "error", "message" -> "Missing Parameters { email | username | password | server }")))
		  	else {
		  	  //instantiate user object
		  	  val u = new Account(email, username, password, server)
		  	 
		  	  //instantiate contact auth actor
		  	  val contactAuthActor = Akka.system.actorOf(Props[ExchangeAuthActor], name="contactAuthActor")
		  	  
		  	  //send the user object to the actor and wait on the result
		  	  val a = (contactAuthActor ? u).mapTo[Boolean].asPromise
		  	  a.await
		  	  
		  	  //got the result
		  	  if(a.value.get)
		  	  {
		  	    //commented by Ami..insertin not needed..only chk that email is present in accounts..valid login
		  		// Account.save(u)
		  		 Ok(toJson(Map("status" -> "ok", "message" -> "User Setup Instantiated")))
		  	  }
		  	  else 
		  	     Ok(toJson(Map("status" -> "error", "message" -> "User Not Authorized")))
		  	}
	}
	/* this method is to check whether the email address 
	 * of the login person is present in accounts 
	 * no changes reqd here..
	 * */
	
	def detail(id: String) = Action {
	  implicit r =>
	    val c = models.Contact.findById(id)
	    if(c.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "email not found")))
	    }
	    else
	    	Ok(toJson(c.head))
	}
	
	
		 def delete = Action(parse.json) {
		 implicit r=>
	     val userId = (r.body \ "userId").asOpt[String].getOrElse("")
	     val contactId = (r.body \ "id").asOpt[String].getOrElse("")
	     //this needs to b editted
	     // val contactDel = Contact.Bind(service, contactId);

	     // Delete the contact and move the deleted contact to the Deleted Items folder. 
	     //contactDel.Delete(DeleteMode.MoveToDeletedItems);
	       
	  	   Ok(toJson(Map("status" -> "success", "message" -> "Contact has been deleted!")))
	       
	  	
	 }
		 /*def update = Action(parse.json) {
		
		 Contact contact = Contact.Bind(service, new ItemId("AAMkA="));

			// Update the contact's surname and company name.
			contact.Surname = "Johnson";
			contact.CompanyName = "Contoso";
			
			// Update the contact's business phone number.
			contact.PhoneNumbers[PhoneNumberKey.BusinessPhone] = "444-444-4444";
			
			// Update the contact's second e-mail address.
			contact.EmailAddresses[EmailAddressKey.EmailAddress2] = new EmailAddress("brian_2@contoso.com");
			
			// Update the contact's first IM address.
			contact.ImAddresses[ImAddressKey.ImAddress1] = "brianIM1@contoso.com";
			
			// Update the contact's business address.
			contact.PhysicalAddresses[PhysicalAddressKey.Business].Street = "4567 Contoso Way";
			contact.PhysicalAddresses[PhysicalAddressKey.Business].City = "Redmond";
			contact.PhysicalAddresses[PhysicalAddressKey.Business].State = "OH";
			contact.PhysicalAddresses[PhysicalAddressKey.Business].PostalCode = "33333";
			contact.PhysicalAddresses[PhysicalAddressKey.Business].CountryOrRegion = "United States";
			
			// Save the contact.
			contact.Update(ConflictResolutionMode.AlwaysOverwrite);
			 
		   }
		   
		   //////////add contact
		 def add = Action(parse.json) {
		 Contact contact = new Contact(service);
			contact.setGivenName("abcde");
			contact.setMiddleName ("xyzd");
			contact.setSurname("Ansari");
			contact.setInitials("Dr");
			contact.setSubject("Contact Details");                                  
			contact.setCompanyName("Creative solution");
			contact.setFileAs(FileAsMapping.SurnameGivenNameMiddleSuffix);
			contact.save();

		 

}*/
		 
	
}