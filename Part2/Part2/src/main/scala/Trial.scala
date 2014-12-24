package main.scala



import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.can.Http
import spray.can.server.Stats
import spray.util._
import spray.http._
import HttpMethods._
import MediaTypes._
import spray.can.Http.RegisterChunkHandler

class Trial extends Actor with ActorLogging {
  implicit val timeout: Timeout = 1.second // for the actor 'asks'
  import context.dispatcher // ExecutionContext for the futures and scheduler

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected => sender ! Http.Register(self)



    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      var serverIp:String="192.168.0.36"
      val serverActor = context.actorSelection("akka.tcp://MasterActor@"+serverIp+":5155/user/Master"+(0))
      serverActor ! "pv"
      println("chk")
      //sender ! HttpResponse(entity = "PONG!")
  }
}
sealed trait Twitsim
case class sendTweetTest(tweet:String) extends Twitsim