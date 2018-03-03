package org.faucet_pipeline.spring.autoconfigure;

import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
@ConditionalOnWebApplication(type = SERVLET)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
class FaucetForWebMvcConfiguration {
    
    @Bean
    WebMvcConfigurer faucetWebMvcConfigurer(
            final FaucetPipelineProperties faucetPipelineProperties, 
            final ResourceProperties resourceProperties, 
            final Manifest manifest
    ) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(final ResourceHandlerRegistry registry) {
                registry.addResourceHandler(faucetPipelineProperties.getPathPatterns())
                        .addResourceLocations(resourceProperties.getStaticLocations())
                        .resourceChain(faucetPipelineProperties.isCacheManifest())
                            .addResolver(new FaucetResourceResolver(manifest));
            }
        };
    }

    @Log
    @RequiredArgsConstructor
    static class FaucetResourceResolver implements ResourceResolver {
        private final Manifest manifest;
        
        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            log.fine(() -> String.format("Resolving resource for request path '%s'", requestPath));
            return Optional.ofNullable(chain.resolveResource(request, requestPath, locations)).orElse(null);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
            log.fine(() -> String.format("Resolving url path for resource '%s'", resourcePath));
            return this.manifest.fetch(resourcePath).orElse(null);
        }
    }
}
