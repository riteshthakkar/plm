package controllers.api

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.immutable._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.mvc.AnyContentAsJson
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.modules.mongodb.jackson.MongoDB
import models.Account
import play.api.libs.ws.WS

@RunWith(classOf[JUnitRunner])
class ContactApiControllerSpec extends Specification {
  	"contact api controller" should { 
			"throw an error in case of invalid parameters" in {
			  val map = Map("Content-Type" -> Seq("application/json"))
			  val content = new AnyContentAsJson(parse("{}"))
			  val result = ContactApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
			  status(result) must equalTo(OK)
			  contentType(result) must beSome("application/json")
			  val data = parse(contentAsString(result))
			  (data \ "status") must be equalTo(toJson("error"))
			}
			
			"return an error response in case of invalid authentication parameters" in {
					running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					  val map = Map("Content-Type" -> Seq("application/json"))
					  val paramMap = Map("username" -> "hdi", "password" -> "", "email" -> "hdhir@grassycreek.nl", "server" -> "https://email.grassycreek.nl/ews/Exchange.asmx" )
					  val content = new AnyContentAsJson(toJson(paramMap))
					  val result = ContactApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
					  status(result) must equalTo(OK)
					  contentType(result) must beSome("application/json")
					  val data = parse(contentAsString(result))
					  (data \ "status") must be equalTo(toJson("error"))
				}
			}
			
			"return a success response in case of valid authentication parameters" in {
					running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
					  val map = Map("Content-Type" -> Seq("application/json"))
					  val paramMap = Map("username" -> "hdir", "password" -> "socialite!", "email" -> "hdhir@grassycreek.nl", "server" -> "https://email.grassycreek.nl/ews/Exchange.asmx" )
					  val content = new AnyContentAsJson(toJson(paramMap))
					  val result = ContactApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
					  
					  // sleep for the thread so that the actor can do its job.
					  Thread.sleep(10000L)
					  status(result) must equalTo(OK)
					  contentType(result) must beSome("application/json")
					  val data = parse(contentAsString(result))
					  (data \ "status") must be equalTo(toJson("ok"))
					  
				}
			}
			
			"return an error response in case of invalid email id" in {
			  running(FakeApplication()) {
			    val result = ContactApiController.detail("eee2o2")(FakeRequest(GET, ""))
			    status(result) must equalTo(OK)
			    contentType(result) must beSome("application/json")
			    val data = parse(contentAsString(result))
			    (data \ "status") must be equalTo(toJson("error"))
			  }
			} 
			
						
		/*			"return a success when email is deleted" in {
			  running(FakeApplication()) {
			  val map = Map("Content-Type" -> Seq("application/json"))
			  val paramMap = Map("userId" -> "hdhir@grassycreek.nl", "email" -> "hdhir@grassycreek.nl")
			  val content = new AnyContentAsJson(toJson(paramMap))
			  val result = ContactApiController.delete(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
			  
			  status(result) must equalTo(OK)
			  contentType(result) must beSome("application/json")
			  val data = parse(contentAsString(result))
			  (data \ "status") must be equalTo(toJson("success"))  
			}
		}*/
	}
}