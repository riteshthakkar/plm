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
}