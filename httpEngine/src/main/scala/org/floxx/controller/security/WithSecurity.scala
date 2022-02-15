package org.floxx.controller.security

import org.floxx.env.service.securityService.SecurityService
import zio.IO
import org.floxx.{FloxxError, UserInfo}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{Challenge, Request, Response}
import zio.Task

trait WithSecurity extends Http4sDsl[Task] {

  def authIOu(req: Request[Task], ss: SecurityService)(success: (Request[Task], UserInfo) => IO[FloxxError,Response[Task]]): Task[Response[Task]]    =
    {
      //println(s"header ${req.headers.get(CaseInsensitiveString("Cookie"))}")
      //req.headers.get(CaseInsensitiveString("Cookie")) match {
      println(s"header ${req.headers.get(Authorization.name)}")
      req.headers.get(Authorization.name) match {
      case None => Unauthorized {
        Challenge("SchemeFloxx", "floxx", Map("Error" -> "None token has been found"))
      }
      case Some(t) =>
        ss.checkAuthentification(t.value.split(" ")(1)).fold(
          Unauthorized(Challenge("SchemeFloxx", "floxx",Map("Erreur" -> "Token invalid") ))) { userInfo =>
          success(req, userInfo)
        }
    }}

  def authIO(req: Request[IO], ss: SecurityService[IO])(success: (Request[IO]) => IO[Response[IO]]): IO[Response[IO]] =
    {

      println(s"header ${req.headers.get(Authorization.name)}")
      req.headers.get(Authorization.name) match {
      case None => Unauthorized(Challenge("SchemeFloxx", "floxx",Map("Error" -> "None token has been found")))
      case Some(t) =>
        ss.checkAuthentification(t.value.split(" ")(1)).fold(Unauthorized(Challenge("SchemeFloxx", "floxx",Map("Erreur" -> "Token invalid") ))) { userInfo =>
          success(req)
        }
    }}
}
