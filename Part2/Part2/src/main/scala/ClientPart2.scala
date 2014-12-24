import scala.collection.mutable.ArrayBuffer
import scala.math._
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import scala.util.Random
import akka.actor.ActorRef
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.pattern.ask
import akka.actor.ActorContext
import akka.actor.Cancellable
import scala.util.matching.Regex
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.immutable.TreeMap

object ClientPart2 extends App
{
var num_of_users=5000
var num_of_actors=8
var systemId=0

var num_of_users1 = 5
var num_of_users2 = 5
var num_of_users3 = 7
var num_of_users4 = 13
var num_of_users5 = 140
var num_of_users6 = 80
var num_of_users7 = 750
/*
var num_of_users1 = 25
var num_of_users2 = 25
var num_of_users3 = 35
var num_of_users4 = 65
var num_of_users5 = 700
var num_of_users6 = 400
var num_of_users7 = 3750
*/
var range1 = args(1).toInt+num_of_users1
var range2 = range1 + num_of_users2
var range3 = range2 + num_of_users3
var range4 = range3 + num_of_users4
var range5 = range4 + num_of_users5
var range6 = range5 + num_of_users6
var range7 = range6 + num_of_users7

var parameter:Int=0
var msg:String = "test"
var node:ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]


var i:Int = 0

val system=ActorSystem("Node")
      def returnparameter(i:Int,startval:Int):Integer = {
        
        if(i>=startval && i<range1)
          parameter=1
        else if(i>=range1 && i<range2)
          parameter=2
        else if(i>=range2 && i<range3)
          parameter=3
        else if(i>=range3 && i<range4)
          parameter=4
        else if(i>=range4 && i<range5)
          parameter=5
        else if(i>=range5 && i<range6)
          parameter=6
        else if(i>=range6 && i<range7)
          parameter=7
          
        return parameter
}
      while(i<num_of_actors){ 
       
        var a:ActorRef=system.actorOf(Props(new Node()),name="Actor"+i)
        node+=a
        i=i+1
       }
     node(0) ! sendActorRef(args(0),node,systemId)
      i=0
      for(i <- 0+args(1).toInt until num_of_users+args(1).toInt)
      {
        node(i%8) ! ServerIp(args(0),i,systemId)
      }
     
     /*
      for(i <- 0+args(1).toInt until num_of_users+args(1).toInt){ 

        parameter = returnparameter(i,args(1).toInt)  
        node(i%8) ! createFollowers(parameter,args(0),i)
        }
    */
      
       
      for(i <- 0+args(1).toInt until num_of_users+args(1).toInt){
      
        parameter = returnparameter(i,args(1).toInt)
        node(i%8) ! sendTweet(args(0),parameter,i,msg,system)
        
        } 
  //for superbowl hash tag - schedule for the last 1 min
      system.scheduler.scheduleOnce(120 seconds){
      for(i <- 0+args(1).toInt until num_of_users+args(1).toInt){
      node(i%8) ! sendTweet(args(0),10,i,msg,system)
      }
      }
     
      
}


class Node extends Actor
{
 
  var count =0
  var startTime=System.currentTimeMillis

  
 def sendreq(serverIp:String,i:Int,msg:String) = {
     val serverActor = context.actorSelection("akka.tcp://MasterActor@"+serverIp+":5155/user/Master"+(i%4))
     
     serverActor ! sendtoFollowers(i,msg)
  }
 def receive = {
   
   case sendActorRef(serverIp,node,systemId) =>
      val serverActor = context.actorSelection("akka.tcp://MasterActor@"+serverIp+":5155/user/Master"+(0))
      serverActor ! registerClientsnode(node,systemId)
      
   case ServerIp(serverIp,clientId,systemId) =>
      println("Registering with server at" + serverIp);
      val serverActor = context.actorSelection("akka.tcp://MasterActor@"+serverIp+":5155/user/Master"+(clientId%4))
      serverActor ! Register(clientId,systemId)
      
   

   case createFollowers(parameter,serverIp,i) =>
     val serverActor = context.actorSelection("akka.tcp://MasterActor@"+serverIp+":5155/user/Master"+(i%4))
      serverActor ! generateFollowers(parameter,i)
   
  case sendTweet(serverIp,parameter,i,msg,system) =>
    
     parameter match{
       
         case 1 => {
           system.scheduler.schedule(0 seconds,0.8 seconds)(sendreq(serverIp,i,msg))
         }
         case 2 => {
           system.scheduler.schedule(0 seconds,3.25 seconds)(sendreq(serverIp,i,msg))
         }
         case 3 =>{
           system.scheduler.schedule(0 seconds,12.5 seconds)(sendreq(serverIp,i,msg))
         }
         case 4 => {
           system.scheduler.schedule(0 seconds,45 seconds)(sendreq(serverIp,i,msg))
         }
         case 5 => {
           system.scheduler.schedule(0 seconds,970 seconds)(sendreq(serverIp,i,msg))
         }
         case 10 => {
           var message = "tweet message #superbowl"
           
           system.scheduler.schedule(0 seconds, 1 seconds)(sendreq(serverIp,i,message))
           
         }
         case _ => {}
         
         

       }
   
   case receiveTweet(msg,clientId) =>
     println("Tweet received by "+ clientId +" "+ msg)  
     
    
 } 
}
 
 
 
sealed trait Twitsim
case class sendActorRef(serverIp:String,node:ArrayBuffer[ActorRef],systemId:Int)
case class registerClientsnode(node:ArrayBuffer[ActorRef],systemId:Int) extends Twitsim
case class ServerIp(value1: String,value2: Int,value3:Int) extends Twitsim
case class Register(clientId:Int,systemId:Int) extends Twitsim
case class createFollowers(value1:Int,value2:String,value3:Int) extends Twitsim
case class generateFollowers(value1:Int,clientId:Int) extends Twitsim
case class sendTweet(value1:String,value2:Int,value3:Int,value4:String,system:ActorSystem) extends Twitsim
case class sendtoFollowers(clientId:Int,value1:String) extends Twitsim
case class receiveTweet(value1:String,clientId:Int) extends Twitsim