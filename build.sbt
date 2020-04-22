import rocks.muki.graphql.schema.GraphQLSchemas

Global / onChangedBuildSource := ReloadOnSourceChanges

val schemaName = "starwars"

/**
 * Aggregates server and client subprojects. This means if you do "compile" or "test" in root, it will run the
 * task for all aggregated subprojects.
 */
lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    inThisBuild(
      List(
        name := "sangria-akka-http-example",
        version := "0.1.0-SNAPSHOT",
        description := "An example GraphQL server written with akka-http, circe and sangria.",
        scalaVersion := "2.12.6",
        scalacOptions ++= Seq("-deprecation", "-feature"),
        libraryDependencies ++= Seq(
          "org.sangria-graphql" %% "sangria" % "1.4.2",
          "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
          "org.sangria-graphql" %% "sangria-circe" % "1.2.1",
          "com.typesafe.akka" %% "akka-http" % "10.1.3",
          "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
          "io.circe" %% "circe-core" % "0.12.3",
          "io.circe" %% "circe-parser" % "0.12.3",
          "io.circe" %% "circe-generic" % "0.12.3",
          "org.scalatest" %% "scalatest" % "3.0.5" % Test,
          "org.scalaj" % "scalaj-http_2.12" % "2.4.2",
        )
      )
    )
  )
  .aggregate(server, client)

lazy val server = (project in file("server"))
  .enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin)
  .settings(
    graphqlSchemas := GraphQLSchemas(
      Vector(
        GraphQLSchema(
          schemaName,
          "A local test schema",
          Def.task {
            val schemaFile = graphqlSchemaGen.value

            GraphQLSchemaLoader.fromFile(schemaFile).loadSchema()
          }.taskValue
        )
      )
    ),
    // generate a schema file
    graphqlSchemaSnippet := "root.SchemaDefinition.StarWarsSchema"
  )

/**
 * Separate subprojects for server and client are currently needed as otherwise you'll have a circular task dependency.
 * (compile -> graphqlCodegen -> graphqlSchemaGen -> run -> compile)
 *
 * It's cleaner to have server/client/shared anyway, if you want to do all in one sbt project.
 */
lazy val client = (project in file("client"))
  .enablePlugins(GraphQLCodegenPlugin)
  .settings(
    graphqlCodegenSchema := (server / graphqlSchemaGen).value,
    sourceDirectories in graphqlCodegen := List(
      sourceDirectory.value / "main" / "graphql" / "queries"
    )
  )
  .dependsOn(server)
