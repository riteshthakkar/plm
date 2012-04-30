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

class Folder(@ObjectId @Id val id: String,
			@BeanProperty @ObjectId @JsonProperty("userId") val userId: String,
			@BeanProperty @JsonProperty("folderName") val folderName: String,
			@BeanProperty @JsonProperty("folderType") val folderType: String,
			@BeanProperty @JsonProperty("exchangeId") val exchangeId: String) {
    	@ObjectId @Id def getId = id
    	def this(uid: String, folderName: String, folderType: String, exchangeId: String) = this(org.bson.types.ObjectId.get.toString, uid, folderName, folderType, exchangeId)
}

object Folder {
    private lazy val db = MongoDB.collection("folders", classOf[Folder], classOf[String])
    def save(e: Folder) { db.save(e) }
    def findById(id: String) = {
      try {
    	  Option(db.findOneById(id))
      }
      catch {
        case e: IllegalArgumentException => Option.empty[Folder]
      }
    }
    
    def exists(uId: String, name: String) = {
      val db = MongoDB.collection("folders", classOf[Folder], classOf[String])
      (db.find().is("uid", uId).is("folderName", name).count >= 1)
    }
    
    def findByUserAndName(uId: String, name: String) = {
      val db = MongoDB.collection("folders", classOf[Folder], classOf[String])
      db.find().is("uid", uId).is("folderName", name).limit(1).toArray().toList.headOption
    }
    
    def findByUser(userId: String) = db.find().is("uid", userId).toArray.toList
    
    def findByUser(userId: String, start: Int, limit: Int) = db.find().is("userId", userId).skip(start).limit(limit).toArray.toList
     
    implicit object FolderReads extends Format[Folder] {
	    def reads(json: JsValue) = new Folder(
	      (json \ "id").as[String],
	      (json \ "uid").as[String],
	      (json \ "folderName").as[String],
	      (json \ "folderType").as[String],
	      (json \ "exchangeId").as[String])
	    
	    def writes(e: Folder) = JsObject(Seq(
	      "id" -> JsString(e.id),
	      "uid" -> JsString(e.userId),
	      "folderName" -> JsString(e.folderName),
	      "folderType" -> JsString(e.folderType),
	      "exchangeId" -> JsString(e.exchangeId)))
    }
    
    def updateFolderId(emailId: String, folderId: String) = {
      
    }
}