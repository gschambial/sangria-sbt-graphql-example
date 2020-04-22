package root

import io.circe.generic.auto._
import io.circe.parser._
import scalaj.http.Http


class ExternalService

object ExternalService {

  //implicit val decoder: Decoder[root.User] = deriveDecoder[root.User]

  def getUser(id: String): User = {
    val response = Http(s"http://localhost:3000/users/$id").asString
    decode[User](response.body) match {
      case Left(_) => null
      case Right(user) => user
    }
  }

  def getUsers(limit: Int, offset: Int): List[User] = {
    val response = Http("http://localhost:3000/users").asString
    decode[List[User]](response.body) match {
      case Left(_) => List.empty
      case Right(users) => users.drop(offset).take(limit)
    }
  }

  def getCompanies(limit: Int, offset: Int): List[Company] = {
    val response = Http("http://localhost:3000/companies").asString
    decode[List[Company]](response.body) match {
      case Left(_) => List.empty
      case Right(companies) => companies.drop(offset).take(limit)
    }
  }

  def getCompany(id: Int): Company = {
    val response = Http(s"http://localhost:3000/companies/$id").asString
    decode[Company](response.body) match {
      case Left(_) => null
      case Right(c) => c
    }
  }
}
