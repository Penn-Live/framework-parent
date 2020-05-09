package io.github.penn.rest;

import io.github.penn.rest.call.RestService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author tangzhongping
 */

@Slf4j
public class RestServiceBeanContext implements ResourceLoaderAware, ApplicationContextAware {

    private ResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;
    private ApplicationContext applicationContext;

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    /**
     * all rest classes
     */
    @Getter
    private Set<Class<?>> restClasses = new LinkedHashSet<>();


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public RestServiceBeanContext init() {
        try {
            log.info("[rest-service] start init rest services");
            restClasses = scanAllRestService();
            log.info("[rest-service] complete init rest services");
        } catch (Exception e) {
            //
        }
        return this;
    }

    /**
     * scan all rest service interface
     */
    private Set<Class<?>> scanAllRestService() {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(getServicePath()) + '/' + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (ifValidRestServiceBean(clazz, className)) {
                            log.info("[rest-service] scan service bean name: {}", className);
                            classSet.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        //skip
                        log.warn("[rest-service] class not found, name: {}, ex: {}", className, e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("scan all restart service error", e);
        }
        return classSet;
    }

    private String getServicePath() {
        String path = getEnvironment().getProperty("framework.rest.service-path");
        return StringUtils.defaultString(path, "com");
    }

    private boolean ifValidRestServiceBean(Class<?> clazz, String className) {
        RestService annotation = clazz.getAnnotation(RestService.class);
        //interface
        if (!clazz.isInterface()) {
            log.warn("[rest-service] class must ben interface, class: {}", clazz);
            return false;
        }
        if (annotation == null) {
            log.warn("[rest-service] class is not RestService Annotated, please check, class: {}", className);
            return false;
        }
        return true;
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.getEnvironment().resolveRequiredPlaceholders(basePackage));
    }


    private Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }


}
