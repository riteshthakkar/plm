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

object ContactApiController extends Controller {
	
	implicit val timeoutContact = Timeout(10 seconds) // needed for `?` below
	
	def setupContact = Action(parse.json) {
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
		  	  val contactAuthActor = Akka.system.actorOf(Props[ExchangeAuthenticationActor], name="contactAuthActor")
		  	  
		  	  //send the user object to the actor and wait on the result
		  	  val a = (contactAuthActor ? u).mapTo[Boolean].asPromise
		  	  a.await
		  	  
		  	  //got the result
		  	  if(a.value.get)
		  	  {
		  		 //Account.save(u)
		  	    val userId = (r.body \ "userId").asOpt[String].getOrElse("")
		  	    val givenName = (r.body \ "givenName").asOpt[String].getOrElse("")
		  	    val fName = (r.body \ "fName").asOpt[String].getOrElse("")
		  	    val lName = (r.body \ "lName").asOpt[String].getOrElse("")
		  	    val displayName = (r.body \ "displayName").asOpt[String].getOrElse("")
		  	    val emailId = (r.body \ "emailId").asOpt[String].getOrElse("")
		  	    val address = (r.body \ "address").asOpt[String].getOrElse("")
		  	    val phoneNos = (r.body \ "phoneNos").asOpt[String].getOrElse("")
		  	    val notes = (r.body \ "notes").asOpt[String].getOrElse("")
		  	    val bday = (r.body \ "bday").asOpt[String].getOrElse("")
		  	    val server = (r.body \ "exchangeId").asOpt[String].getOrElse("")
		  	    val server = (r.body \ "folderId").asOpt[String].getOrElse("")
		  		 Ok(toJson(Map("status" -> "ok", "message" -> "User Setup Instantiated")))
		  	  }
		  	  else 
		  	     Ok(toJson(Map("status" -> "error", "message" -> "User Not Authorized")))
		  	}
	}
	
	def detail(id: String) = Action {
	  implicit r =>
	    val e = Contact.findById(id) //Latest Code for "Contact.scala" To be taken from Ami.
	    if(e.isEmpty) {
	      Ok(toJson(Map("status" -> "error", "message" -> "Contact not found")))
	    }
	    else
	    	Ok(toJson(e.head))
	}
	


}