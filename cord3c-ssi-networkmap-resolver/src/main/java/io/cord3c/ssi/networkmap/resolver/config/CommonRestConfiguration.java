package io.cord3c.ssi.networkmap.resolver.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.spring.setup.boot.core.CrnkBootConfigurer;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.core.CrnkTomcatAutoConfiguration;
import io.crnk.spring.setup.boot.data.facet.CrnkFacetAutoConfiguration;
import io.crnk.spring.setup.boot.format.PlainJsonFormatAutoConfiguration;
import io.crnk.spring.setup.boot.home.CrnkHomeAutoConfiguration;
import io.crnk.spring.setup.boot.jpa.CrnkJpaAutoConfiguration;
import io.crnk.spring.setup.boot.meta.CrnkMetaAutoConfiguration;
import io.crnk.spring.setup.boot.monitor.CrnkSpringOpenTracingAutoConfiguration;
import io.crnk.spring.setup.boot.security.CrnkSecurityAutoConfiguration;
import io.crnk.spring.setup.boot.ui.CrnkUIAutoConfiguration;
import io.crnk.spring.setup.boot.validation.CrnkValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import({
		CommonRuntimeConfiguration.class, JacksonAutoConfiguration.class, PlainJsonFormatAutoConfiguration.class,

		CrnkCoreAutoConfiguration.class, CrnkFacetAutoConfiguration.class, CrnkHomeAutoConfiguration.class,
		CrnkJpaAutoConfiguration.class, CrnkMetaAutoConfiguration.class, CrnkSecurityAutoConfiguration.class,
		CrnkSpringOpenTracingAutoConfiguration.class, CrnkUIAutoConfiguration.class, CrnkValidationAutoConfiguration.class,
		CrnkTomcatAutoConfiguration.class,

		EmbeddedWebServerFactoryCustomizerAutoConfiguration.class, ServletWebServerFactoryAutoConfiguration.class,
})
@EnableScheduling
public class CommonRestConfiguration {

	@Bean
	public CrnkBootConfigurer crnkBootConfigurer() {
		return boot -> {
			// we want a bit more compact by avoiding almost redundant self and related relationship links
			DocumentMappingConfig documentMappingConfig = boot.getModuleRegistry().getDocumentMappingConfig();
			documentMappingConfig.getResourceMapping().setSerializeSelfRelationshipLinks(false);

			boot.getObjectMapper().registerModule(new JavaTimeModule());
			boot.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		};
	}
}
