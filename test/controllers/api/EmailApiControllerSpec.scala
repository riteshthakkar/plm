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

@RunWith(classOf[JUnitRunner])
class EmailApiControllerSpec extends Specification {
	"email api controller" should { 
		"throw an error in case of invalid parameters" in {
		  val map = Map("Content-Type" -> Seq("application/json"))
		  val content = new AnyContentAsJson(parse("{}"))
		  val result = EmailApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
		  status(result) must equalTo(OK)
		  contentType(result) must beSome("application/json")
		  val data = parse(contentAsString(result))
		  (data \ "status") must be equalTo(toJson("error"))
		}
		
		"return a success response in case of valid parameters" in {
		  val map = Map("Content-Type" -> Seq("application/json"))
		  val paramMap = Map("username" -> "hdhir@grassycreek.nl", "password" -> "socialite!", "email" -> "hdhir@grassycreek.nl", "server" -> "https://email.grassycreek.nl/ews/Exchange.asmx" )
		  val content = new AnyContentAsJson(toJson(paramMap))
		  val result = EmailApiController.setup(FakeRequest(POST, "", new play.api.test.FakeHeaders(map), content.asJson.head))
		  status(result) must equalTo(OK)
		  contentType(result) must beSome("application/json")
		  val data = parse(contentAsString(result))
		  (data \ "status") must be equalTo(toJson("ok"))
		}
	}
	
	
}

//	  
//	  contentType(result) 
//	  charset(result) must beSome("utf-8")
//	  contentAsString(result) must contain("Hello Bob")