package models

import org.codehaus.jackson.annotate.JsonProperty
import reflect.BeanProperty
import javax.persistence.Id
import play.api.Play.current
import play.modules.mongodb.jackson.MongoDB
import scala.collection.JavaConversions._
import net.vz.mongodb.jackson.ObjectId

class Email(@ObjectId @Id val id: String,
			@BeanProperty @JsonProperty("uid") val userId: String,
			@BeanProperty @JsonProperty("from") val from: String,
			@BeanProperty @JsonProperty("to") val to: List[String],
			@BeanProperty @JsonProperty("cc") val cc: List[String],
			@BeanProperty @JsonProperty("bcc") val bcc: List[String],
			@BeanProperty @JsonProperty("subject") val subject: String) {
    	@ObjectId @Id def getId = id
    	def this(uid: String, from: String, to: List[String], cc: List[String], bcc: List[String], subject: String) = this(org.bson.types.ObjectId.get.toString, uid, from, to, cc, bcc, subject)
}

object Email {
    private lazy val db = MongoDB.collection("emails", classOf[Email], classOf[String])
    def save(e: Email) { db.save(e) }
    def findById(id: String) = Option(db.findOneById(id))
    def findByUser(userId: String) = db.find().is("uid", userId).toArray.toList
}