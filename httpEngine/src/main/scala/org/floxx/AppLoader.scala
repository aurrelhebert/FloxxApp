package org.floxx

import cats.effect.IO
import org.floxx.controller.io.{TechnicalApi, _}
import org.floxx.repository.postgres.{AuthRepoPg, CfpRepoPg, HitRepoCfg}
import org.floxx.service._

object AppLoader {

  trait AppContext {
    def cfpApi: TrackApi
    def hitApi: HitApi
    def securityApi: SecurityApi
    def technicalApi: TechnicalApi
    def securityService: SecurityService[IO]
  }

  private case class ApplicationContext(
      cfpApi: TrackApi,
      hitApi: HitApi,
      securityApi: SecurityApi,
      technicalApi: TechnicalApi,
      securityService: SecurityService[IO]
  ) extends AppContext

  final def initialize: AppContext = {

    val rpg: CfpRepoPg                       = new CfpRepoPg()
    val hrpg: HitRepoCfg                     = new HitRepoCfg()
    val srpg: AuthRepoPg                     = new AuthRepoPg()
    val trackService: TrackService[IO]       = new TrackServiceImpl(rpg)
    val hitService: HitService[IO]           = new HitServiceImpl(trackService, hrpg)
    val securityService: SecurityService[IO] = new SecurityServiceImpl(srpg)
    val adminService:AdminService[IO] = new AdminServiceImpl((rpg))


    ApplicationContext(
      TrackApi(trackService, securityService),
      HitApi(hitService, securityService),
      SecurityApi(securityService),
      TechnicalApi(adminService,securityService),
      securityService
    )
  }

}
