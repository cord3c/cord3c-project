import com.google.cloud.tools.jib.gradle.JibPlugin
import org.gradle.api.Incubating
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.tasks.Exec
import org.gradle.authentication.http.HttpHeaderAuthentication

@Incubating
class PublishingDefaultsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.with {
            apply plugin: 'maven-publish'

				/*
            // allow login to docker (define task in root to avoid repeated logins)
            def rootTasks = rootProject.tasks
            if (!rootTasks.getAsMap().containsKey("dockerLogin")) {
                def registryHost = DOCKER_REGISTRY.substring(0, DOCKER_REGISTRY.indexOf("/"))
                def rootDockerLogin = rootTasks.create("dockerLogin", Exec)
                rootDockerLogin.commandLine 'docker', 'login', registryHost, '--username', dockerPublishUser, '--password', dockerPublishPass
            }
            def dockerLogin = tasks.create("dockerLogin")
            dockerLogin.dependsOn(rootProject.tasks.getByName('dockerLogin'))

            // for development purposes we must only publish docker images for deployment
            if (ciBuild || System.getenv("CI_VERSION") != null) {
                publishing {
                    repositories {
                        maven {
                            url = MAVEN_REPOSITORY_URL

                            // https://gitlab.com/help/user/project/packages/maven_repository.md
                            // https://stackoverflow.com/questions/51137958/gitlab-ci-secret-variables-for-gradle-publish

                            if (mavenTokenHttpHeaderValue != null) {
                                credentials(HttpHeaderCredentials) {
                                    name = mavenTokenHttpHeaderName
                                    value = mavenTokenHttpHeaderValue
                                }
                                authentication {
                                    header(HttpHeaderAuthentication)
                                }
                            } else if (mavenPublishUser != null) {
                                credentials {
                                    username = mavenPublishUser
                                    password = mavenPublishPass
                                }
                            }
                        }
                    }
                }
            }

            afterEvaluate {
                plugins.withType(JibPlugin) {
                    jib {
                        to {
                            auth {
                                username = dockerPublishUser
                                password = dockerPublishPass
                            }
                        }
                    }
                }
            }

				 */
        }
    }
}
