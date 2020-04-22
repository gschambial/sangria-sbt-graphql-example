package root

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Argument, EnumType, EnumValue, Field, IntType, InterfaceType, ListType, ObjectType, OptionInputType, OptionType, Schema, StringType, fields, interfaces}

import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {
  /**
   * Resolves the lists of characters. These resolutions are batched and
   * cached for the duration of a query.
   */
  val characters = Fetcher.caching(
    (ctx: CharacterRepo, ids: Seq[String]) ⇒
      Future.successful(ids.flatMap(id ⇒ ctx.getHuman(id) orElse ctx.getDroid(id))))(HasId(_.id))

  //val externalService = root.ExternalService

  val EpisodeEnum = EnumType(
    "Episode",
    Some("One of the films in the Star Wars Trilogy"),
    List(
      EnumValue("NEWHOPE",
        value = Episode.NEWHOPE,
        description = Some("Released in 1977.")),
      EnumValue("EMPIRE",
        value = Episode.EMPIRE,
        description = Some("Released in 1980.")),
      EnumValue("JEDI",
        value = Episode.JEDI,
        description = Some("Released in 1983."))))

  val Character: InterfaceType[CharacterRepo, Character] =
    InterfaceType(
      "Character",
      "A character in the Star Wars Trilogy",
      () ⇒ fields[CharacterRepo, Character](
        Field("id", StringType,
          Some("The id of the character."),
          resolve = _.value.id),
        Field("name", OptionType(StringType),
          Some("The name of the character."),
          resolve = _.value.name),
        Field("friends", ListType(Character),
          Some("The friends of the character, or an empty list if they have none."),
          resolve = ctx ⇒ characters.deferSeqOpt(ctx.value.friends)),
        Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
          Some("Which movies they appear in."),
          resolve = _.value.appearsIn map (e ⇒ Some(e)))
      ))

  val Human =
    ObjectType(
      "Human",
      "A humanoid creature in the Star Wars universe.",
      interfaces[CharacterRepo, Human](Character),
      fields[CharacterRepo, Human](
        Field("id", StringType,
          Some("The id of the human."),
          resolve = _.value.id),
        Field("name", OptionType(StringType),
          Some("The name of the human."),
          resolve = _.value.name),
        Field("friends", ListType(Character),
          Some("The friends of the human, or an empty list if they have none."),
          resolve = ctx ⇒ characters.deferSeqOpt(ctx.value.friends)),
        Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
          Some("Which movies they appear in."),
          resolve = _.value.appearsIn map (e ⇒ Some(e))),
        Field("homePlanet", OptionType(StringType),
          Some("The home planet of the human, or null if unknown."),
          resolve = _.value.homePlanet)
      ))

  val Droid = ObjectType(
    "Droid",
    "A mechanical creature in the Star Wars universe.",
    interfaces[CharacterRepo, Droid](Character),
    fields[CharacterRepo, Droid](
      Field("id", StringType,
        Some("The id of the droid."),
        resolve = _.value.id),
      Field("name", OptionType(StringType),
        Some("The name of the droid."),
        resolve = ctx ⇒ Future.successful(ctx.value.name)),
      Field("friends", ListType(Character),
        Some("The friends of the droid, or an empty list if they have none."),
        resolve = ctx ⇒ characters.deferSeqOpt(ctx.value.friends)),
      Field("appearsIn", OptionType(ListType(OptionType(EpisodeEnum))),
        Some("Which movies they appear in."),
        resolve = _.value.appearsIn map (e ⇒ Some(e))),
      Field("primaryFunction", OptionType(StringType),
        Some("The primary function of the droid."),
        resolve = _.value.primaryFunction)
    ))

  val ID = Argument("id", StringType, description = "id of the character")

  val EpisodeArg = Argument("episode", OptionInputType(EpisodeEnum),
    description = "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode.")

  val LimitArg = Argument("limit", OptionInputType(IntType), defaultValue = 20)
  val OffsetArg = Argument("offset", OptionInputType(IntType), defaultValue = 0)

  val UserType = ObjectType(
    "User",
    fields[Unit, User](
      Field("id", StringType,
        resolve = _.value.id),
      Field("name", StringType,
        resolve = _.value.name),
      Field("age", IntType,
        resolve = _.value.age),
      // Field("companyId", IntType,
      //  resolve = _.value.companyId)
      Field("company", CompanyType,
        resolve = ctx => ExternalService.getCompany(ctx.value.companyId))
    )
  )

  lazy val CompanyType = ObjectType(
    "Company",
    fields[Unit, Company] (
      Field("id", IntType,
        resolve = _.value.id),
      Field("name", StringType,
        resolve = _.value.name),
      Field("description", StringType,
        resolve = _.value.description)
    )
  )

  val Query = ObjectType(
    "Query",
    fields[CharacterRepo, Unit](
      Field("hero", Character,
        arguments = EpisodeArg :: Nil,
        deprecationReason = Some("Use `human` or `droid` fields instead"),
        resolve = (ctx) ⇒ ctx.ctx.getHero(ctx.arg(EpisodeArg))),
      Field("human", OptionType(Human),
        arguments = ID :: Nil,
        resolve = ctx ⇒ ctx.ctx.getHuman(ctx arg ID)),
      Field("droid", Droid,
        arguments = ID :: Nil,
        resolve = ctx ⇒ ctx.ctx.getDroid(ctx arg ID).get),
      Field("humans", ListType(Human),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.getHumans(ctx arg LimitArg, ctx arg OffsetArg)),
      Field("droids", ListType(Droid),
        arguments = LimitArg :: OffsetArg :: Nil,
        resolve = ctx ⇒ ctx.ctx.getDroids(ctx arg LimitArg, ctx arg OffsetArg)),
      Field("user", UserType,
        arguments = ID :: Nil,
        resolve = ctx ⇒ ExternalService.getUser(ctx arg ID)),
      Field("users", ListType(UserType),
        arguments = Argument("limit", OptionInputType(IntType), defaultValue = 20) :: OffsetArg :: Nil,
        resolve = ctx ⇒ ExternalService.getUsers(ctx arg LimitArg, ctx arg OffsetArg)),
      Field("companies", ListType(CompanyType),
        arguments = Argument("limit", OptionInputType(IntType), defaultValue = 20) :: OffsetArg :: Nil,
        resolve = ctx ⇒ ExternalService.getCompanies(ctx arg LimitArg, ctx arg OffsetArg))
    ))

  val StarWarsSchema = Schema(Query)
}
