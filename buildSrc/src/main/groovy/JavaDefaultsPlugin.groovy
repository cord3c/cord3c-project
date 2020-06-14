import java.text.DecimalFormat

import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoMerge
import org.gradle.testing.jacoco.tasks.JacocoReport

@Incubating
class JavaDefaultsPlugin implements Plugin<Project> {

	public static final String VENDOR = "Cord3c"

	@Override
	void apply(Project project) {
		project.with {
			apply plugin: 'java-library'
			apply plugin: 'jacoco'
			apply plugin: 'org.unbroken-dome.test-sets'
			apply plugin: 'idea'

			java {
				withSourcesJar()
			}

			tasks.withType(JavaCompile) {
				// required by Corda to perform reflection on constructor parameters
				options.compilerArgs << "-parameters"
			}

			// https://dzone.com/articles/reproducible-builds-in-java
			tasks.withType(AbstractArchiveTask) {
				preserveFileTimestamps = false
				reproducibleFileOrder = true
			}

			tasks.withType(Jar) { jar ->
				jar.manifest {
					attributes(
							'Implementation-Title': project.name,
							'Implementation-Vendor': VENDOR
					)
				}

				jar.version = null
			}

			configurations {
				bom
				compile.extendsFrom bom
			}

			dependencies {
				implementation platform("org.springframework.boot:spring-boot-dependencies:$SPRING_BOOT_VERSION")
				implementation platform("io.crnk:crnk-bom:$CRNK_VERSION")
				annotationProcessor platform("io.crnk:crnk-bom:$CRNK_VERSION")

				// https://github.com/mapstruct/mapstruct/issues/1581 -> order matters: mapstruct before lombok
				compileOnly "org.mapstruct:mapstruct-processor:$MAPSTRUCT_VERSION"
				annotationProcessor "org.mapstruct:mapstruct-processor:$MAPSTRUCT_VERSION"

				compileOnly "org.projectlombok:lombok:$LOMBOK_VERSION"
				testCompileOnly "org.projectlombok:lombok:$LOMBOK_VERSION"
				annotationProcessor "org.projectlombok:lombok:$LOMBOK_VERSION"
				testAnnotationProcessor "org.projectlombok:lombok:$LOMBOK_VERSION"

				compileOnly 'com.google.auto.service:auto-service:1.0-rc4'
				annotationProcessor 'com.google.auto.service:auto-service:1.0-rc4'

				testImplementation "org.junit.jupiter:junit-jupiter-api:$JUNIT_VERSION"
				testImplementation "org.junit.jupiter:junit-jupiter-params:$JUNIT_VERSION"
				testRuntime "org.junit.jupiter:junit-jupiter-engine:$JUNIT_VERSION"

				testImplementation 'org.springframework.boot:spring-boot-test-autoconfigure'
				testImplementation 'org.assertj:assertj-core'
				testImplementation 'org.springframework:spring-test'
				testImplementation 'org.mockito:mockito-core'
				testImplementation 'ch.qos.logback:logback-classic'
			}

			tasks.withType(Test) {
				useJUnitPlatform()

				testLogging {
					exceptionFormat = 'full'
					showStandardStreams = false
				}

				maxHeapSize = "1100M"
			}

			tasks.withType(Test) {
				// we cannot analyze duplicate classes from v2 and not interested in system tests
				// w3c not our code base as of yet
				if (!it.name.startsWith("systemTest") && !it.project.name.contains("w3c") && !it.project.name.contains("v2")) {
					addToJacocoRootReport(project, it)
				}
			}
		}

		configureIntegrationTests project
	}

	private void configureIntegrationTests(Project project) {
		project.with {
			testSets {
				integrationTest
			}

			integrationTest {
				group = 'Verification'
				description 'Run self-contained integration tests.'
				dependencies {
					integrationTestImplementation sourceSets.test.output
				}
			}
			// check should run all local verification tasks
			tasks.check.dependsOn tasks.integrationTest

			// disabling useless unconfigured jar
			integrationTestJar {
				description '[DISABLED] Assembles a jar archive containing the integration test classes.'
				enabled = false
				group = null
			}

			test {
				outputs.file new File(buildDir, "jacoco/test.exec")
			}

			idea {
				module {
					testSourceDirs += project.sourceSets.integrationTest.java.srcDirs
					testSourceDirs += project.sourceSets.integrationTest.resources.srcDirs
				}
			}

			/*sonarqube {
				properties {
					properties["sonar.tests"] = (sourceSets.test.allSource.srcDirs +
							sourceSets.integrationTest.allSource.srcDirs).findAll { it.exists() }
					property 'sonar.jacoco.reportPaths', project.rootProject.tasks.jacocoMerge.destinationFile
					property 'sonar.junit.reportPaths', 'build/test-results/test,build/test-results/integrationTest'
				}
			}*/
		}
	}

	private void addToJacocoRootReport(Project project, Test testTask) {
		def rootProject = project.rootProject
		if (!rootProject.tasks.any { it.name == "jacocoMerge" }) {
			rootProject.with {
				apply plugin: 'base'
				apply plugin: 'jacoco'
				def jacocoMerge = task(type: JacocoMerge, "jacocoMerge", {
					doFirst {
						executionData = files(executionData.findAll { it.exists() })
					}
				})
				task(type: JacocoReport, group: 'verification', "jacocoRootReport", {
					description = 'Generates an aggregate report from all subprojects'
					dependsOn jacocoMerge

					additionalSourceDirs.from files()
					sourceDirectories.from files()
					classDirectories.from files()

					executionData jacocoMerge.destinationFile
					doLast {
						def reportPath = reports.html.entryPoint
						logger.lifecycle("report at: " + reportPath.toURI())
						def matcher = (reportPath.text =~ /<tfoot>.*?"bar">([0-9,]+) of ([0-9,]+)<.*<\/tfoot>/)[0]
						def covered = matcher[1].replace(',', '') as Double
						def total = matcher[2].replace(',', '') as Double
						def percentage = new DecimalFormat("#.00").format(covered / total * 100d)
						logger.lifecycle("total line coverage: $percentage%")
					}
				})
			}
		}
		rootProject.tasks.jacocoMerge.with {
			dependsOn testTask
			executionData testTask
		}
		rootProject.tasks.jacocoRootReport.with {
			additionalSourceDirs.from(project.sourceSets.main.allSource.srcDirs)
			sourceDirectories.from(project.sourceSets.main.allSource.srcDirs)
			classDirectories.from(project.sourceSets.main.output)
		}
	}

}
