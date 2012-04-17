package com.plm.pcm

import com.typesafe.play.mini._
import play.api.mvc._
import play.api.mvc.Results._
import microsoft.exchange.webservices.data._
import scala.collection.JavaConversions._
import java.net.URI

/**
 * this application is registered via Global
 */
object App extends Application { 
  
   val service = new ExchangeService();
 
        
  def route = {
    case GET(Path("/coco")) & QueryString(qs) => Action{ request=>
      println(request.body)
      println(play.api.Play.current)
      val result = QueryString(qs,"foo").getOrElse("noh")
      Ok(<h1>It works!, query String {result}</h1>).as("text/html")
    }
    
    case GET(Path("/setup")) => Action {
      request => Ok("")
    }
   
//    case GET(Path("/email")) => Action {
//      request => 
//      	val credentials = new WebCredentials("hdir", "socialite!", "")
//  		service.setCredentials(credentials)
//  		service.setUrl(new URI("https://email.grassycreek.nl/ews/Exchange.asmx"))
// 
//        val view = new ItemView(50)
//        val results = service.findItems(WellKnownFolderName.Inbox, view)
//        results.getItems().foreach { i => i.load() 
//          println(i.getSubject()) }
//        
//        Ok("test")
//      
//    }
    
  }
}
