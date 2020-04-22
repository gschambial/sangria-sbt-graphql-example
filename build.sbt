name := "sangria-akka-http-example"
version := "0.1.0-SNAPSHOT"

description := "An example GraphQL server written with akka-http, circe and sangria."

scalaVersion := "2.12.6"
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.2",
  "org.sangria-graphql" %% "sangria-slowlog" % "0.1.8",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1",

  "com.typesafe.akka" %% "akka-http" % "10.1.3",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",

  "io.circe" %%	"circe-core" % "0.12.3",
  "io.circe" %% "circe-parser" % "0.12.3",
  "io.circe" %% "circe-generic" % "0.12.3",

  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scalaj" % "scalaj-http_2.12" % "2.4.2",
)

// generate a schema file
graphqlSchemaSnippet := "root.SchemaDefinition.StarWarsSchema"
target in graphqlSchemaGen := new File("src/main/graphql/schema")

// Validating queries
sourceDirectory in (Test, graphqlValidateQueries) := new File("src/main/graphql/queries")

// telling Codegen plugin to use this schema file
// graphqlCodegenSchema := new File("src/main/graphql/schema/schema.graphql")

graphqlCodegenSchema := graphqlRenderSchema.toTask("build").value

enablePlugins(GraphQLSchemaPlugin, GraphQLQueryPlugin, GraphQLCodegenPlugin)

Revolver.settings
enablePlugins(JavaAppPackaging)
