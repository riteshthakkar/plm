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
import models.Account
import microsoft.exchange.webservices.data._
import java.net.URI
import play.modules.mongodb.jackson.MongoDB
import java.util.Date

object ContactApiController extends Controller {

  implicit val timeout1 = Timeout(10 seconds)

  val service = new ExchangeService()

  def setup = Action(parse.json) {
    implicit r =>
      val email = (r.body \ "email").asOpt[String].getOrElse("")
      val username = (r.body \ "username").asOpt[String].getOrElse("")
      val password = (r.body \ "password").asOpt[String].getOrElse("")
      val server = (r.body \ "server").asOpt[String].getOrElse("")

      if (email.isEmpty || username.isEmpty || password.isEmpty || server.isEmpty)
        Ok(toJson(Map("status" -> "error", "message" -> "Missing Parameters { email | username | password | server }")))
      else {
        //instantiate user object
        val u = new Account(email, username, password, server)

        //instantiate contact auth actor
        val contactAuthActor = Akka.system.actorOf(Props[ExchangeAuthActor], name = "contactAuthActor")

        //send the user object to the actor and wait on the result
        val a = (contactAuthActor ? u).mapTo[Boolean].asPromise
        a.await

        //got the result
        if (a.value.get) {
          //commented by Ami..insertin not needed..only chk that email is present in accounts..valid login
          // Account.save(u)
          Ok(toJson(Map("status" -> "ok", "message" -> "User Setup Instantiated")))
        } else
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
      if (c.isEmpty) {
        Ok(toJson(Map("status" -> "error", "message" -> "email not found")))
      } else
        Ok(toJson(c.head))
  }

  /*******************func to delete contact *******************************/

  def delete = Action(parse.json) {
    implicit r =>
      val userId = (r.body \ "userId").asOpt[String].getOrElse("")
      val contactId = (r.body \ "id").asOpt[String].getOrElse("")

      val contactDel = Contact.bind(service, new ItemId(contactId))
      contactDel.delete(DeleteMode.HardDelete)
      Ok(toJson(Map("status" -> "success", "message" -> "Contact has been deleted!")))

  }
  /*******************func to edit contact *******************************/

  def update = Action(parse.json) {

    implicit r =>

      val userId = (r.body \ "userId").asOpt[String].getOrElse("")
      val contactId = (r.body \ "id").asOpt[String].getOrElse("")

      val givenName = (r.body \ "givenName").asOpt[String].getOrElse("")
      val fName = (r.body \ "fName").asOpt[String].getOrElse("")
      val lName = (r.body \ "lName").asOpt[String].getOrElse("")
      val displayName = (r.body \ "displayName").asOpt[String].getOrElse("")
      val emailId1 = (r.body \ "emailId1").asOpt[String].getOrElse("")
      val emailId2 = (r.body \ "emailId2").asOpt[String].getOrElse("")

      val streetB = (r.body \ "streetB").asOpt[String].getOrElse("")
      val cityB = (r.body \ "cityB").asOpt[String].getOrElse("")
      val stateB = (r.body \ "stateB").asOpt[String].getOrElse("")
      val postalcodeB = (r.body \ "postalcodeB").asOpt[String].getOrElse("")
      val countryB = (r.body \ "countryB").asOpt[String].getOrElse("")

      val streetH = (r.body \ "streetH").asOpt[String].getOrElse("")
      val cityH = (r.body \ "cityH").asOpt[String].getOrElse("")
      val stateH = (r.body \ "stateH").asOpt[String].getOrElse("")
      val postalcodeH = (r.body \ "postalcodeH").asOpt[String].getOrElse("")
      val countryH = (r.body \ "countryH").asOpt[String].getOrElse("")

      val phoneWork = (r.body \ "phoneWork").asOpt[Int].getOrElse(0)
      val phoneHome = (r.body \ "phoneHome").asOpt[Int].getOrElse(0)

      val bday = (r.body \ "bday").asOpt[String].getOrElse("")

      //Bind to an existing meeting request by using its unique identifier.
      val contactVal = Contact.bind(service, new ItemId(userId))

      contactVal.setGivenName(givenName)
      contactVal.setNickName(fName)
      contactVal.setSurname(lName)
      contactVal.setDisplayName(displayName)
      //bdayDate=conObj.getBday()
      val bdayDate = new Date()
      bdayDate.setDate(bday.toInt)
      contactVal.setBirthday(bdayDate)
      val exchangeId = (r.body \ "exchangeId").asOpt[String].getOrElse("")
      val folderId = (r.body \ "folderId").asOpt[String].getOrElse("")
      // update the contact.
      contactVal.update(ConflictResolutionMode.AlwaysOverwrite)
      val con = new models.Contact(userId, givenName, fName, lName, displayName, emailId1, emailId2, streetB, cityB, stateB, postalcodeB, countryH, streetH, cityH, stateH, postalcodeH, countryH, phoneWork, phoneHome, bday /*,HasPicture*/ , exchangeId, folderId)
      models.Contact.save(con);
      Ok(toJson(Map("status" -> "ok", "message" -> "contact Updated")))

  }

  ///////*********Func to add contact***********///////
  def add = Action(parse.json) {
    implicit r =>

      val contactVal = new Contact(service)

      val userId = (r.body \ "userId").asOpt[String].getOrElse("")
      val contactId = (r.body \ "id").asOpt[String].getOrElse("")
      val givenName = (r.body \ "givenName").asOpt[String].getOrElse("")
      val fName = (r.body \ "fName").asOpt[String].getOrElse("")
      val lName = (r.body \ "lName").asOpt[String].getOrElse("")
      val displayName = (r.body \ "displayName").asOpt[String].getOrElse("")
      val emailId1 = (r.body \ "emailId1").asOpt[String].getOrElse("")
      val emailId2 = (r.body \ "emailId2").asOpt[String].getOrElse("")

      val streetB = (r.body \ "streetB").asOpt[String].getOrElse("")
      val cityB = (r.body \ "cityB").asOpt[String].getOrElse("")
      val stateB = (r.body \ "stateB").asOpt[String].getOrElse("")
      val postalcodeB = (r.body \ "postalcodeB").asOpt[String].getOrElse("")
      val countryB = (r.body \ "countryB").asOpt[String].getOrElse("")

      val streetH = (r.body \ "streetH").asOpt[String].getOrElse("")
      val cityH = (r.body \ "cityH").asOpt[String].getOrElse("")
      val stateH = (r.body \ "stateH").asOpt[String].getOrElse("")
      val postalcodeH = (r.body \ "postalcodeH").asOpt[String].getOrElse("")
      val countryH = (r.body \ "countryH").asOpt[String].getOrElse("")

      val phoneWork = (r.body \ "phoneWork").asOpt[Int].getOrElse(0)
      val phoneHome = (r.body \ "phoneHome").asOpt[Int].getOrElse(0)

      val bday = (r.body \ "bday").asOpt[String].getOrElse("")

      /*   val HasPicture = (r.body \ "HasPicture").asOpt[Boolean].getOrElse("")*/
      val exchangeId = (r.body \ "exchangeId").asOpt[String].getOrElse("")
      val folderId = (r.body \ "folderId").asOpt[String].getOrElse("")

      contactVal.setGivenName(givenName)
      contactVal.setNickName(fName)
      contactVal.setSurname(lName)
      contactVal.setDisplayName(displayName)

      val bdayDate = new Date()
      bdayDate.setDate(bday.toInt)
      contactVal.setBirthday(bdayDate)
      contactVal.save(WellKnownFolderName.Contacts)
      val con = new models.Contact(userId, givenName, fName, lName, displayName, emailId1, emailId2, streetB, cityB, stateB, postalcodeB, countryH, streetH, cityH, stateH, postalcodeH, countryH, phoneWork, phoneHome, bday /*,HasPicture*/ , exchangeId, folderId)
      models.Contact.save(con);
      Ok(toJson(Map("status" -> "ok", "message" -> "contact saved")))
  }

}