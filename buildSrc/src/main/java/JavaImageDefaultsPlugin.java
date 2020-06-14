import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.cloud.tools.jib.gradle.JibExtension;
import org.apache.commons.io.output.NullOutputStream;
import org.gradle.api.Incubating;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.process.ExecResult;

@Incubating
public class JavaImageDefaultsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().apply("com.google.cloud.tools.jib");

		JibExtension jib = project.getExtensions().getByType(JibExtension.class);

		TaskContainer tasks = project.getTasks();

		// assemble all the artifacts for the image
		Configuration runtimeClasspath = project.getConfigurations().getByName("runtimeClasspath");
		Task assembleImage = tasks.create("assembleImage");
		assembleImage.dependsOn("processResources");
		assembleImage.dependsOn("compileJava");
		assembleImage.dependsOn(runtimeClasspath);

		TaskDependency jarDependency = runtimeClasspath.getTaskDependencyFromProjectDependency(true, "jar");
		assembleImage.dependsOn(jarDependency);

		// Automatically add the image ID as an additional tag for the docker push.
		ImageIdAsTagTask imageIdAsTag = tasks.create("jibImageIdAsTag", ImageIdAsTagTask.class);
		imageIdAsTag.dependsOn(assembleImage);

		Task jibTask = tasks.getByName("jib");
		Task jibDockerBuild = tasks.getByName("jibDockerBuild");
		Task jibBuildTar = tasks.getByName("jibBuildTar");
		jibTask.dependsOn(imageIdAsTag);
		jibDockerBuild.dependsOn(imageIdAsTag);
		jibBuildTar.dependsOn(imageIdAsTag);

		jibTask.onlyIf(task -> {
			String imageId = imageIdAsTag.getImageId();
			String image = jib.getTo().getImage();
			String tag = image + ":" + imageId;
			return isImageUpToDate(project, tag);
		});
	}

	private boolean isImageUpToDate(Project project, String image) {
		// see https://stackoverflow.com/questions/30543409/how-to-check-if-a-docker-image-with-a-specific-tag-exist-locally
		// direct jib support would be nice at some point to avoid docker usage...
		ExecResult result = project.exec(execSpec -> {
			HashMap<String, String> env = new HashMap<>(System.getenv());
			env.put("DOCKER_CLI_EXPERIMENTAL", "enabled");
			execSpec.setEnvironment(env);
			execSpec.setErrorOutput(new NullOutputStream());
			execSpec.setStandardOutput(new NullOutputStream());
			execSpec.setIgnoreExitValue(true);
			execSpec.setCommandLine(Arrays.asList("docker", "manifest", "inspect", image));
		});
		return result.getExitValue() != 0;
	}
}
