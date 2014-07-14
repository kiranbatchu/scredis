package scredis.io

import com.typesafe.scalalogging.slf4j.LazyLogging

import akka.actor._

import scredis.Transaction
import scredis.exceptions._
import scredis.protocol._
import scredis.protocol.requests.ConnectionRequests.Quit
import scredis.protocol.requests.ServerRequests.Shutdown

import scala.util.Try
import scala.collection.mutable.{ Map => MMap }
import scala.concurrent.{ ExecutionContext, Future, Await }
import scala.concurrent.duration._

import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

trait Connection {
  protected implicit val ec: ExecutionContext = ExecutionContext.global
  protected def send[A](request: Request[A]): Future[A]
}

trait BlockingConnection {
  protected def sendBlocking[A](request: Request[A]): A
}

trait TransactionEnabledConnection extends Connection {
  protected def sendTransaction(transaction: Transaction): Future[IndexedSeq[Try[Any]]]
}

object Connection {
  private val ids = MMap[String, AtomicInteger]()
  
  private[io] def getUniqueName(name: String): String = {
    val counter = ids.synchronized {
      ids.getOrElseUpdate(name, new AtomicInteger(0))
    }
    s"$name-${counter.incrementAndGet()}"
  }
  
}