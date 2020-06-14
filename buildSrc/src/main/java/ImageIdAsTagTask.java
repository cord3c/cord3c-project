import com.google.cloud.tools.jib.gradle.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Unfortunately JIB does not allow to quickly compute the image ID without publishing or building a tar file. Nor is jib able to
 * add a tag after it has been build. For this reason, we tag the image with a custom image id computed from all the Gradle
 * inputs. This allows for quick access to an image id.
 * <p>
 * Note that we cannot make use of the image digest. It includes the repository name and as such changes when mirror or moved to
 * another repository. This is necessary to have a stable content-addressed identifier across different docker repositories. The
 * docker repository protocol is a bit weird in that it includes the tag/repository name in the computation of the "digest" hash.
 * That means that one and the same image ID will receive different digest values for for different repositories/hashes. To bake
 * a stable identifier into the image, the images are tagged with the image id.
 */
public class ImageIdAsTagTask extends DefaultTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageIdAsTagTask.class);

	private String imageId;

	@TaskAction
	public void run() {
		Project project = getProject();
		JibExtension jibExt = project.getExtensions().getByType(JibExtension.class);

		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

		long s = System.currentTimeMillis();
		MessageDigest md = getMessageDigest();
		digest(md, jibExt.getContainer());
		digest(md, jibExt.getExtraDirectories());
		digest(md, jibExt.getFrom());
		digest(md, jibExt.getContainerizingMode());
		digest(md, project.getConfigurations().getByName("runtimeClasspath"));
		digest(md, sourceSets.getByName("main"));
		imageId = Hex.encodeHexString(md.digest());
		LOGGER.debug("computed digest in {}ms", System.currentTimeMillis() - s);

		// Need to create new set because Jib is Google software and Google really likes immutable data structrues.
		Set<String> oldTags = jibExt.getTo().getTags();
		Set<String> newTags = new HashSet<>(oldTags);
		newTags.add(imageId);
		jibExt.getTo().setTags(newTags);

		// save to file for reuse/caching purposes
		try {
			FileUtils.write(new File(project.getBuildDir(), "image-input.id"), "sha256:" + imageId, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Internal
	public String getImageId() {
		if (imageId == null) {
			throw new IllegalStateException("imageId not available yet");
		}
		return imageId;
	}

	private void digest(MessageDigest md, SourceSet sourceSet) {
		// note that projects may not have classes or resources
		digest(md, sourceSet.getOutput().getDirs().getFiles(), true);
		digest(md, sourceSet.getOutput().getFiles(), true);
	}

	private void digest(MessageDigest md, Configuration configuration) {
		digest(md, configuration.getFiles());
	}

	private void digest(MessageDigest md, Collection<File> files) {
		digest(md, files, false);
	}

	private void digest(MessageDigest md, Collection<File> files, boolean ignoreMissing) {
		List<File> sorted = new ArrayList<>(files);
		Collections.sort(sorted, (o1, o2) -> {
			// we only compare the name, not the directory where it is coming from
			// to maintain the digest across platform
			return o1.getName().compareTo(o2.getName());
		});
		for (File file : sorted) {
			if (ignoreMissing && !file.exists()) {
				continue;
			}
			digest(md, file);
		}
	}

	private void digest(MessageDigest md, ContainerParameters container) {
		digest(md, container.getAppRoot());
		digest(md, container.getMainClass());
		digest(md, container.getUser());
		digest(md, container.getWorkingDirectory());
		digest(md, container.getExtraClasspath());
		digest(md, container.getArgs());
		digest(md, container.getAppRoot());
		digest(md, container.getEntrypoint());
		digest(md, container.getEnvironment());
		digest(md, container.getJvmFlags());
		digest(md, container.getLabels());
		digest(md, container.getPorts());
		digest(md, container.getVolumes());
	}

	private void digest(MessageDigest md, ExtraDirectoriesParameters extraDirectories) {
		digest(md, extraDirectories.getPaths().size());
		for (ExtraDirectoryParameters extraDirParams : extraDirectories.getPaths()) {
			// default extra directory may not exists
			Path from = extraDirParams.getFrom();
			digest(md, extraDirParams.getInto());
			if (from.toFile().exists()) {
				digest(md, from.toFile());
			}
		}
	}

	private void digest(MessageDigest md, Path path) {
		File file = path.toFile();
		digest(md, file);
	}

	private void digest(MessageDigest md, File file) {
		LOGGER.debug("digesting file {}", file.getName());
		digest(md, file.getName());
		if (file.isDirectory()) {
			LOGGER.debug("  digesting directory \"{}\"", file.getName());
			digest(md, Arrays.asList(file.listFiles()));
			LOGGER.debug("  digested directory \"{}\"", file.getName());
		} else {

			try {
				byte[] buffer = new byte[4000];
				try (InputStream is = new FileInputStream(file); DigestInputStream dis = new DigestInputStream(is, md)) {
					while (dis.read(buffer) != -1) {
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private void digest(MessageDigest md, Map<String, String> map) {
		LOGGER.debug("digesting map");
		if (map != null) {
			digest(md, map.size());

			List<String> keys = new ArrayList(map.keySet());
			Collections.sort(keys);
			for (String key : keys) {
				digest(md, key);
				digest(md, map.get(key));
			}
		} else {
			md.update((byte) 0);
		}
	}

	private void digest(MessageDigest md, List<String> args) {
		if (args != null) {
			digest(md, args.size());
			for (String arg : args) {
				digest(md, arg);
			}
		} else {
			md.update((byte) 0);
		}
	}

	private void digest(MessageDigest md, int value) {
		LOGGER.debug("digesting int {}", value);
		md.update(ByteBuffer.allocate(4).putInt(value).array());
	}

	private void digest(MessageDigest md, BaseImageParameters from) {
		digest(md, from.getImage());
	}

	private void digest(MessageDigest md, String value) {
		LOGGER.debug("digesting string {}", value);
		if (value != null) {
			md.update(value.getBytes());
		} else {
			md.update((byte) 0);
		}
	}

	private MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
