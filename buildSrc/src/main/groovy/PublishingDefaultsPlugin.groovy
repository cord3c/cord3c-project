import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

@Incubating
class PublishingDefaultsPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		project.with {
			apply plugin: 'maven-publish'

			apply plugin: 'com.jfrog.bintray'

			// Create the pom configuration:
			def pomConfig = {
				licenses {
					license {
						name "The Apache Software License, Version 2.0"
						url "http://www.apache.org/licenses/LICENSE-2.0.txt"
						distribution "repo"
					}
				}
				developers {
					developer {
						id 'cord3c.io'
						name 'cord3c.io'
						email 'info@cord3c.io'
					}
				}

				scm {
					url "https://github.com/cord3c/cord3c-project"
				}
			}


			def bom = project.name == 'cord3c-bom'
			if (!bom) {



				publishing {
					publications {
						mavenJava(MavenPublication) {
							from components.java





							/*artifact sourcesJar {
								classifier "sources"
							}

							artifact javadocJar {
								classifier "javadoc"
							}*/

							groupId GROUP_ID
							artifactId project.name
							version rootProject.version

							pom.withXml {
								def root = asNode()
								root.appendNode('description', 'cord3c SSI, HTTP, Rest, monitoring support for Corda')
								root.appendNode('name', 'cord3c')
								root.appendNode('url', 'https://www.cord3c.io')
								root.children().last() + pomConfig
							}
						}
					}
				}
			}

			def releaseBuild = project.hasProperty('stable')

			bintray {
				user = System.env.BINTRAY_USER
				key = System.env.BINTRAY_TOKEN

				publications = ['mavenJava']

				pkg {
					repo = releaseBuild ? 'maven' : 'mavenLatest'
					name = project.name
					userOrg = 'cord3c'
					licenses = ['Apache-2.0']
					vcsUrl = 'https://github.com/cord3c/cord3c-project.git'
					websiteUrl = 'http://www.cord3c.io'
					desc = 'cord3c SSI, HTTP, Rest, monitoring support for Corda'
					labels = ['corda', 'rest', 'http', 'ssi', 'did', 'vc']

					githubRepo = 'cord3c/cord3c-project'
					githubReleaseNotesFile = 'README.md'

					publish = !releaseBuild

					version {
						name = project.version
						desc = 'cord3c SSI, HTTP, Rest, monitoring support for Corda'
						released = new Date()
						vcsTag = "v$project.version"

						/*
						mavenCentralSync {
							sync = false //[Default: true] Determines whether to sync the version to Maven Central.
							user = System.env.NEXUS_DEPLOY_USER
							password = System.env.NEXUS_DEPLOY_PASS
							close = '1'
						}*/
					}


				}
			}

			tasks.bintrayUpload.dependsOn assemble, publishToMavenLocal

			publish.dependsOn tasks.bintrayUpload

		}
	}
}
