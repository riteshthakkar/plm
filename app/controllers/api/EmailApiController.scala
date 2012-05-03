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

object EmailApiController extends Controller {
	
	implicit val timeout = Timeout(10 seconds) // needed for `?` below
	
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
		  	 
		  	  //instantiate email auth actor
		  	  val emailAuthActor = Akka.system.actorOf(Props[ExchangeAuthenticationActor], name="emailAuthActor")
		  	  
		  	  //send the user object to the actor and wait on the result
		  	  val a = (emailAuthActor ? u).mapTo[Boolean].asPromise
		  	  a.await
		  	  
		  	  //got the result
		  	  if(a.value.get)
		  	  {
		  		 Account.save(u)
		  		 Ok(toJson(Map("status" -> "ok", "message" -> "User Setup Instantiated")))
		  	  }
		  	  else 
		  	     Ok(toJson(Map("status" -> "error", "message" -> "User Not Authorized")))
		  	}
	}
	
	def detail(id: String) = Action {
	  implicit r =>
	    val e = Email.findById(id)
	    if(e.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "email not found")))
	    }
	    else
	    	Ok(toJson(e.head))
	}
	
	def recent(email: String, start: Int, limit: Int) = Action {
	  implicit r =>
	    //instantiate email auth actor
		val emailApiActor = Akka.system.actorOf(Props[EmailApiActor], name="emailApiActor")
	  	val a = (emailApiActor ? Recent(email, start, limit)).mapTo[List[Email]].asPromise
	  	a.await
	  	
	  	val emails = a.value.get
	  	
	  	if(!emails.isEmpty) {
	  		Ok(toJson(emails))
	  	}
	  	else {
	  	  Ok(toJson(Map("status" -> "error", "message" -> "error accessing emails!")))
	  	}
	}
	 def send = Action(parse.json) {
		 	implicit r =>
	  		//val userId=(r.body \ "userId").asOpt[String].getOrElse("")
	  		val from=(r.body \ "from").asOpt[String].getOrElse("")
	  		val to =(r.body \ "to").asOpt[String].getOrElse("")
	  		val cc =(r.body \ "cc").asOpt[String].getOrElse("")
	  		val bcc =(r.body \ "bcc").asOpt[String].getOrElse("")
	  		val subject=(r.body \ "subject").asOpt[String].getOrElse("")
	  		val body=(r.body \ "body").asOpt[String].getOrElse("")
	    
	    	val service = new ExchangeService()
	  		val a = Account.findByEmail(from)

	    
	    if(a.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "invalid user id")))
	    }
	    else {

	    	val account = a.head
	    	
	    	val credentials = new WebCredentials(account.username,account.password,"")
	  		service.setCredentials(credentials)
	  		service.setUrl(new URI(account.serverURI))
	  		
	  		//send an email thru the exchange server
	    	val sentItems = microsoft.exchange.webservices.data.Folder.bind(service, WellKnownFolderName.SentItems);
	    	val sFolder = !models.Folder.exists(account.id, "sent") match {
	    	  				case true => new models.Folder(account.id, sentItems.getDisplayName(), "sent", sentItems.getId().getUniqueId()) 
	    	  				case _ => models.Folder.findByUserAndName(account.id, "sent").head
	    				}
	    	
	    	models.Folder.save(sFolder)
	    	
	    	
	  		
		    val email = new EmailMessage(service)
		    email.getToRecipients.add(EmailAddress.getEmailAddressFromString(to))
		    email.getCcRecipients.add(EmailAddress.getEmailAddressFromString(cc))
		    email.setSubject(subject)
		    email.setFrom(EmailAddress.getEmailAddressFromString(from))
		    email.setBody(MessageBody.getMessageBodyFromText(body))
		     
		    email.send()

		    //print("ERROR 1 : "+ email.getInternetMessageId().toString())
		    //print("ERROR 2 : "+ email.getDateTimeSent().getTime())
		    // save the email in our local mongo instance in the "sent" folder
		    val e = new Email(account.id, from, to :: Nil, cc :: Nil, bcc :: Nil, subject, body, email.getInternetMessageId().toString(), email.getDateTimeSent().getTime(), sFolder.id)
	    	Email.save(e)
	    	
	    	
		    Ok(toJson(Map("status" -> "success", "message" -> "Email has been sent and saved!")))
	    }
	  }
	 
	 def delete = Action(parse.json) {
	   implicit r=>
	     val userId = (r.body \ "userId").asOpt[String].getOrElse("")
	     val emailId = (r.body \ "emailId").asOpt[String].getOrElse("")
	     
	     val service = new ExchangeService()
	  	 val a = Account.findById(userId)
	  	 val emailBox = Email.findById(emailId)
	  	 
	  	 if(a.isEmpty || emailBox.isEmpty){
	  	   Ok(toJson(Map("status" -> "error", "message" -> "invalid parameters")))
	  	 }
	  	 else{
	  	   val existingEmail = emailBox.head
	  	   
	  	   val account = a.head
	  	   val deletedItems = microsoft.exchange.webservices.data.Folder.bind(service, WellKnownFolderName.DeletedItems);
	       val dFolder = !models.Folder.exists(account.id, "deleted") match {
	         case true => new models.Folder(account.id, deletedItems.getDisplayName(), "deleted", deletedItems.getId().getUniqueId())
	         case _ => models.Folder.findByUserAndName(account.id, "deleted").head
	    	}
	       models.Folder.save(dFolder)
	       
	       val credentials = new WebCredentials(account.username,account.password,"")
	  	   service.setCredentials(credentials)
	  	   service.setUrl(new URI(account.serverURI))
	       
	  	   
	  	   //move to deleted items
	  	   val message = EmailMessage.bind(service, ItemId.getItemIdFromString(existingEmail.exchangeId))
	  	   message.delete(DeleteMode.MoveToDeletedItems)
	       
	  	   //updating the FolderId
	  	   
	       val e = new Email(existingEmail.id, userId, existingEmail.from, existingEmail.to, existingEmail.cc, existingEmail.bcc, existingEmail.subject, existingEmail.body, existingEmail.exchangeId, existingEmail.sentDate, dFolder.id)
	       Email.save(e)
	       
	        Ok(toJson(Map("status" -> "success", "message" -> "Email has been deleted!")))
	       
	  	 }
	 }
}