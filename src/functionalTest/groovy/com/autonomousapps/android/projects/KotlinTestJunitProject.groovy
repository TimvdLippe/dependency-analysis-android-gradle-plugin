package com.autonomousapps.android.projects

import com.autonomousapps.AbstractProject
import com.autonomousapps.kit.*
import com.autonomousapps.model.Advice
import com.autonomousapps.model.ProjectAdvice

import static com.autonomousapps.AdviceHelper.*
import static com.autonomousapps.kit.Dependency.*

final class KotlinTestJunitProject extends AbstractProject {

  final GradleProject gradleProject
  private final String agpVersion

  KotlinTestJunitProject(String agpVersion) {
    this.agpVersion = agpVersion
    this.gradleProject = build()
  }

  private GradleProject build() {
    def builder = newGradleProjectBuilder()
    builder.withRootProject { r ->
      r.gradleProperties = GradleProperties.minimalAndroidProperties()
      r.withBuildScript { bs ->
        bs.buildscript = BuildscriptBlock.defaultAndroidBuildscriptBlock(agpVersion)
      }
    }
    builder.withAndroidSubproject('app') { subproject ->
      subproject.sources = appSources
      subproject.withBuildScript { buildScript ->
        buildScript.plugins = [Plugin.androidAppPlugin, Plugin.kotlinAndroidPlugin]
        buildScript.dependencies = [
          kotlinTestJunit('androidTestImplementation'),
          junit('androidTestImplementation'),
          appcompat('implementation'),
        ]
      }
    }

    def project = builder.build()
    project.writer().write()
    return project
  }

  private appSources = [
    new Source(
      SourceType.KOTLIN, 'App', 'com/example',
      """\
        package com.example
      
        class App {
          fun magic() = 42
        }
      """.stripIndent()
    ),
    new Source(
      SourceType.KOTLIN, 'Test', 'com/example',
      """\
        package com.example
      
        import org.junit.Assert.assertTrue
        import org.junit.Test
      
        class Test {
          @Test fun test() {
            assertTrue(true)
          }
        }
      """.stripIndent(),
      'androidTest'
    )
  ]

  Set<ProjectAdvice> actualBuildHealth() {
    return actualProjectAdvice(gradleProject)
  }

  private static ProjectAdvice app() {
    projectAdviceForDependencies(':app', changeKotlinTestJunit())
  }

  private static Set<Advice> changeKotlinTestJunit() {
    return [Advice.ofChange(
      moduleCoordinates('org.jetbrains.kotlin:kotlin-test-junit', Plugin.KOTLIN_VERSION),
      'androidTestImplementation',
      'androidTestRuntimeOnly'
    )]
  }

  Set<ProjectAdvice> expectedBuildHealth = [app()]
}
