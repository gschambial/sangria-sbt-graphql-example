package root

object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}

trait Character {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Episode.Value]
}

case class Human(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Episode.Value],
  homePlanet: Option[String]) extends Character

case class Droid(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Episode.Value],
  primaryFunction: Option[String]) extends Character

case class User(id: String, name: String, age: Int, companyId: Int)

case class Company(id: Int, name: String, description: String)
