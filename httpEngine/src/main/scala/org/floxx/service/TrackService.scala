package org.floxx.service

import cats.effect.IO
import org.floxx.IOVal
import org.floxx.config.Config
import org.floxx.model.jsonModel.Slot
import org.floxx.repository.postgres.CfpRepoPg
import org.floxx.utils.floxxUtils._
import org.slf4j.{ Logger, LoggerFactory }
import play.api.libs.json.Json

trait TrackService[F[_]] {

  def readDataFromCfpDevoxx(): F[IOVal[Int]]
  def loadActiveSlotIds(isActiveFunction: Slot => Boolean): F[IOVal[Set[Slot]]]
  def loadSlot(id: String): F[IOVal[Option[Slot]]]
  def roomById(id: String): F[IOVal[Option[String]]]

}

class TrackServiceImpl(repoPg: CfpRepoPg) extends TrackService[IO] with WithTransact {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val urlCfp = Config.cfp.url
  val urlByDay = Config.cfp.days.map(d => {
    val url = s"${urlCfp}${d}"
    url
  })

  import org.http4s.client.blaze._

  import scala.concurrent.ExecutionContext.Implicits.global

  override def readDataFromCfpDevoxx(): IO[IOVal[Int]] = {

    def s(url: String): IO[List[Slot]] =
      BlazeClientBuilder[IO](global).resource.use { client =>
        import org.floxx.model.jsonModel._

        client.get(url) { r =>
          r.as[String].map { rt =>
            val j = Json.parse(rt)

            (j \ "slots").as[List[Slot]]
          }
        }
      }

    logger.debug("read from CFP")

    import cats.implicits._

    for {

      slots <- urlByDay.map(s).traverse(identity).map(_.fold(Nil)((a, b) => a ::: b))

      nbLine <- run(repoPg.addSlots(computeRoomKey(slots)))

    } yield nbLine

  }

  override def loadActiveSlotIds(isActiveFilter: Slot => Boolean): IO[IOVal[Set[Slot]]] =
    (for {
      slots <- run(repoPg.allSlotIds).eitherT
    } yield slots.filter(isActiveFilter)).value

  override def loadSlot(id: String): IO[IOVal[Option[Slot]]] =
    (for {
      slot <- run(repoPg.getSlotById(id)).eitherT
    } yield slot).value

  override def roomById(id: String): IO[IOVal[Option[String]]] =
    IO(Right(Config.rooms.roomsMapping(id)))

  private def computeRoomKey(slots: List[Slot]): List[Slot] =
    slots
      .filter(_.talk.isDefined)
      .flatMap(s => {
        Config.rooms.roomsMapping(s.roomId).map { r =>
          val sId = s"${s.day}_${s.roomId}_${s.fromTime}-${s.toTime}"
          s.copy(slotId = sId, roomId = r)
        }
      })

}