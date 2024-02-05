package org.floxx.env.service

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import org.floxx.domain.AuthUser
import org.floxx.domain.AuthUser.{ Login, Mdp }
import org.floxx.domain.User.SimpleUser
import org.floxx.domain.error.{ AuthentificationError, FloxxError }
import org.floxx.env.api.ApiTask
import org.floxx.env.configuration.config.{ Configuration, GlobalConfig }
import org.floxx.env.repository.userRepository.UserRepo
import org.floxx.UserInfo
import org.http4s.circe.jsonOf
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.interop.catz._
import zio.{ RLayer, Task, _ }

object securityService {

  //utilisateur déjà identifié TODO à renommer !! par AuthenticatedUSer <- mettre dans le token en enlevant des champs et ajoutant d'autres
  val layer: RLayer[UserRepo with Configuration, SecurityService] =
    ZLayer {
      for {
        userRepo <- ZIO.service[UserRepo]
        conf <- ZIO.service[Configuration]
      } yield SecurityServiceImpl(userRepo, conf)
    }

  case class AuthenticatedUser(name: String, token: String, isAdmin: Boolean = false)

  object AuthenticatedUser {

    implicit val format = jsonOf[ApiTask, AuthenticatedUser]
  }

  trait SecurityService {
    def readUserById(userId: SimpleUser.Id): IO[FloxxError,Option[AuthUser]]
    def authentification(user: SimpleUser.Id, mdp: Mdp): IO[AuthentificationError,AuthenticatedUser]
    def checkAuthentification(token: String): IO[AuthentificationError, UserInfo]

  }

  case class SecurityServiceImpl(userRepo: UserRepo, conf: Configuration) extends SecurityService {

    override def readUserById(userId: SimpleUser.Id): IO[FloxxError,Option[AuthUser]] =
      userRepo.userByUserId(userId)

    override def checkAuthentification(token: String): IO[AuthentificationError, UserInfo] =
      for {
        config <- conf.getConf.mapError(err => AuthentificationError(err.msg))
        jwtClaim <- ZIO
          .fromTry(
            Jwt.decode(
              token,
              config.floxx.secret,
              Seq(JwtAlgorithm.HS256)
            )
          )
          .mapError(err => AuthentificationError(err.getMessage))
        userInfo <- ZIO.fromEither(decode[UserInfo](jwtClaim.content)).mapError(err => AuthentificationError(err.getMessage))

      } yield userInfo

    override def authentification(user: SimpleUser.Id, mdp: Mdp): IO[AuthentificationError,AuthenticatedUser] =
      for {
        config <- conf.getConf.mapError(err => AuthentificationError(err.msg))
        userFound <- userRepo.userByUserId(user).mapError(err => AuthentificationError(err.msg))
        auth <-
          userFound .fold(ZIO.fail(AuthentificationError("login or pass is invalid")))(u => {
            if(u.mdp == mdp) {
              ZIO.succeed(  AuthenticatedUser(s"${u.firstName.value} ${u.lastName.value}",
                tokenGenerator(
                  UserInfo(
                    u.userId.getOrElse(SimpleUser.Id("no ID")),
                    u.login,
                    u.isAdmin
                  ),
                  config
                ),
                u.isAdmin
              ))
            }else{
              ZIO.fail(AuthentificationError("login or pass is invalid"))
            }


          })





      }yield auth
      }
    private def tokenGenerator(info: UserInfo, conf: GlobalConfig): String =
      Jwt.encode(
        info.asJson.noSpaces,
        conf.floxx.secret,
        JwtAlgorithm.HS256
      )
  }

  def authentification(user: SimpleUser.Id, mdp: Mdp): RIO[SecurityService, AuthenticatedUser] =
    ZIO.serviceWithZIO[SecurityService](_.authentification(user, mdp))
  def checkAuthentification(token: String): RIO[SecurityService, Option[UserInfo]] =
    ZIO.serviceWithZIO[SecurityService](_.checkAuthentification(token))
  def loadUserById(userId: SimpleUser.Id): RIO[SecurityService, Option[AuthUser]] =
    ZIO.serviceWithZIO[SecurityService](_.readUserById(userId))

}
