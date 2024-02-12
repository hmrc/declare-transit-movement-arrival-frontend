import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "declare-transit-movement-arrival-frontend"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .settings(DefaultBuildSettings.scalaSettings: _*)
  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(majorVersion := 0)
  .settings(headerSettings(IntegrationTest): _*)
  .settings(automateHeaderSettings(IntegrationTest))
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq("models._", "models.OptionBinder._"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.play.views.html.helpers._",
      "uk.gov.hmrc.play.views.html.layouts._",
      "models.Mode",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 9483,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting  := true,
    scalacOptions ++= Seq("-feature", "-Wconf:src=routes/.*:s"),
    libraryDependencies ++= AppDependencies(),
    dependencyOverrides ++= AppDependencies.overrides,
    retrieveManaged := true,
    scalaVersion    := "2.13.12",
    resolvers ++= Seq(Resolver.jcenterRepo),
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("lib/govuk-frontend/govuk/all.js", "lib/hmrc-frontend/hmrc/all.js", "javascripts/ctc.js"))
    ),
    uglifyCompressOptions         := Seq("unused=false", "dead_code=false", "warnings=false"),
    Assets / pipelineStages       := Seq(concat, uglify),
    ThisBuild / useSuperShell     := false,
    ThisBuild / scalafmtOnCompile := true
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf",
    "-Dlogger.resource=logback-test.xml"
  )
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test" / "generators"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork              := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf",
    "-Dlogger.resource=logback-it.xml"
  )
)
