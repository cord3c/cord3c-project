package net.corda.plugins

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider

import javax.inject.Inject

class QuasarExtension {

	final ListProperty<String> excludePackages

	final Provider<String> exclusions

	@Inject
	QuasarExtension(ObjectFactory objects) {
		excludePackages = objects.listProperty(String)
		exclusions = excludePackages.map { excludes ->
			excludes.isEmpty() ? '' : "=x(${excludes.join(';')})".toString()
		}
		excludePackages.addAll(["antlr**", "bftsmart**", "co.paralleluniverse**", "com.codahale**", "com.esotericsoftware**", "com.fasterxml**", "com.google**", "com.ibm**", "com.intellij**", "com.jcabi**", "com.nhaarman**", "com.opengamma**", "com.typesafe**", "com.zaxxer**", "de.javakaffee**", "groovy**", "groovyjarjarantlr**", "groovyjarjarasm**", "io.atomix**", "io.github**", "io.netty**", "jdk**", "junit**", "kotlin**", "net.bytebuddy**", "net.i2p**", "org.apache**", "org.assertj**", "org.bouncycastle**", "org.codehaus**", "org.crsh**", "org.dom4j**", "org.fusesource**", "org.h2**", "org.hamcrest**", "org.hibernate**", "org.jboss**", "org.jcp**", "org.joda**", "org.junit**", "org.mockito**", "org.objectweb**", "org.objenesis**", "org.slf4j**", "org.w3c**", "org.xml**", "org.yaml**", "reflectasm**", "rx**", "org.jolokia**"])
	}
}
