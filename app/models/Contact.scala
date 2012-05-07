package models

import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import javax.persistence.Id
import play.api.Play.current
import play.modules.mongodb.jackson.MongoDB
import scala.collection.JavaConversions._
import net.vz.mongodb.jackson.ObjectId
import play.api.libs.json._
import play.api.libs.json.Json._

class Contact(@ObjectId @Id val id: String,
    		@BeanProperty @JsonProperty("userId") val userId: String,
			@BeanProperty @JsonProperty("givenName") val givenName: String,
			@BeanProperty @JsonProperty("fName") val fName: String,
			@BeanProperty @JsonProperty("lName") val lName: String,
			@BeanProperty @JsonProperty("displayName") val displayName: String,
			@BeanProperty @JsonProperty("emailId1") val emailId1: String,
			@BeanProperty @JsonProperty("emailId2") val emailId2: String,
			//BUSINESS ADDRESS
			@BeanProperty @JsonProperty("StreetB") val streetB: String,
			@BeanProperty @JsonProperty("cityB") val cityB: String,
			@BeanProperty @JsonProperty("stateB") val stateB: String,
			@BeanProperty @JsonProperty("postalcodeB") val postalcodeB: String,
			@BeanProperty @JsonProperty("countryB") val countryB: String,
			//HOME ADDRESS
			
			@BeanProperty @JsonProperty("StreetH") val streetH: String,
			@BeanProperty @JsonProperty("cityH") val cityH: String,
			@BeanProperty @JsonProperty("stateH") val stateH: String,
			@BeanProperty @JsonProperty("postalcodeH") val postalcodeH: String,
			@BeanProperty @JsonProperty("countryH") val countryH: String,
//			PHONE NOS
			@BeanProperty @JsonProperty("phoneWork") val phoneWork: Int,
			@BeanProperty @JsonProperty("phoneHome") val phoneHome: Int,
			@BeanProperty @JsonProperty("bday") val bday: String,
			//@BeanProperty @JsonProperty("HasPicture") val HasPicture: Boolean,
			@BeanProperty @JsonProperty("exchangeId") val exchangeId: String,
			@BeanProperty @JsonProperty("folderId") val folderId: String) {
    		@ObjectId @Id def getId = id
    		def this(userId: String,givenName: String,fName:String, lName: String, displayName: String,emailId1: String,emailId2: String,streetB: String,cityB: String,stateB: String,postalcodeB: String,countryB: String,streetH: String,cityH: String,stateH: String,postalcodeH: String,countryH: String,phoneWork: Int,phoneHome: Int,bday: String,/*HasPicture:Boolean,*/exchangeId:String, folderId:String)  
    		= this(org.bson.types.ObjectId.get.toString, userId,givenName,fName,lName, displayName, emailId1,emailId2,streetB,cityB,stateB,postalcodeB,countryH,streetH,cityH,stateH,postalcodeH,countryH,phoneWork,phoneHome,bday/*,HasPicture*/,exchangeId,folderId)
}

object Contact {
    private lazy val db = MongoDB.collection("contacts", classOf[Contact], classOf[String])
    def save(c: Contact) { db.save(c) }
    def findById(id: String) = {
      try {
    	  Option(db.findOneById(id))
      }
      catch {
        case c: IllegalArgumentException => Option.empty[Contact]
      }
    }
    def findByUser(userId: String) = db.find().is("userId", userId).toArray.toList
    
    /*def findByUser(userId: String, start: Int, limit: Int) = {
      val db = MongoDB.collection("contacts", classOf[Contact], classOf[String])
      db.find().is("userId", userId).skip(start).limit(limit).toArray.toList
    }*/
     
    implicit object ContactReads extends Format[Contact] {
	    def reads(json: JsValue) = new Contact(
	        
	         
	      (json \ "userId").as[String],
	      (json \ "givenName").as[String],
	      (json \ "fName").as[String],
	      (json \ "lName").as[String],
	      (json \ "displayName").as[String],
	      (json \ "emailId1").as[String],
	      (json \ "emailId2").as[String],
	      (json \ "streetB").as[String],
	      (json \ "cityB").as[String],
	      (json \ "stateB").as[String],
	      (json \ "postalcodeB").as[String],
	      (json \ "countryB").as[String],
	      (json \ "streetH").as[String],
	      (json \ "cityH").as[String],
	      (json \ "stateH").as[String],
	      (json \ "postalcodeH").as[String],
	      (json \ "countryH").as[String],
	      (json \ "phoneWork").as[Int],
	      (json \ "phoneHome").as[Int],
	      (json \ "bday").as[String],
	      //  (json \ "HasPicture").as[Boolean],
	      (json \ "exchangeId").as[String],
	      (json \ "folderId").as[String]
	     )
	    
	    

	    def writes(c: Contact) = JsObject(Seq(
	      "id" -> JsString(c.id),
	      "userId" -> JsString(c.userId),
	      "givenName" -> JsString(c.givenName),
	      "fName" -> JsString(c.fName),
	      "lName" -> JsString(c.lName),
	      "displayName" -> JsString(c.displayName),
	      "emailId1" -> JsString(c.emailId1),
	      "emailId2" -> JsString(c.emailId2),
	      "streetH" -> JsString(c.streetH),
	      "cityH" -> JsString(c.cityH),
	      "stateH" -> JsString(c.stateH),
	      "postalcodeH" -> JsString(c.postalcodeH),
	      "countryH" -> JsString(c.countryH),
	      "streetB" -> JsString(c.streetB),
	      "cityB" -> JsString(c.cityB),
	      "stateB" -> JsString(c.stateB),
	      "postalcodeB" -> JsString(c.postalcodeB),
	      "countryB" -> JsString(c.countryB),
	      "phoneWork" -> JsNumber(c.phoneWork),
	      "phoneHome" -> JsNumber(c.phoneHome),
	      "bday" -> JsString(c.bday),
	      //      "HasPicture" -> JsBoolean(c.HasPicture),
	      "exchangeId" -> JsString(c.exchangeId),
	      "folderId" -> JsString(c.folderId)))
    }
}