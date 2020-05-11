package io.github.penn.rest;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.penn.rest.call.RestService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
import org.springframework.util.CollectionUtils;

/**
 * @author tangzhongping
 */

@Slf4j
public class RestServiceBeanContext implements ResourceLoaderAware, ApplicationContextAware {

    private ResourcePatternResolver resourcePatternResolver;
    private MetadataReaderFactory metadataReaderFactory;
    private ApplicationContext applicationContext;

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private String PACKAGE_FORMAT=
        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +"%s"+"/" + DEFAULT_RESOURCE_PATTERN;

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
            e.printStackTrace();
        }
        return this;
    }

    /**
     * scan all rest service interface
     */
    private Set<Class<?>> scanAllRestService() {
        Set<Class<?>> classSet = new LinkedHashSet<>();
        List<String> serviceClassPath = resolveBasePackage();
        for (String path : serviceClassPath) {
            classSet.addAll(loadConditionClass(path,this::ifValidRestServiceBean));
        }
        log.info("[rest-service] load all rest service interface: {}",classSet);
        return classSet;
    }

    /**
     * load rest service
     * @param path
     * @return
     */
    private Set<Class<?>> loadConditionClass(String path, Predicate<Class<?>> clazzPredicate) {
        Set<Class<?>> classSet = Sets.newHashSet();
        try {
            Resource[] resources = this.resourcePatternResolver.getResources(path);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazzPredicate.test(clazz)) {
                            classSet.add(clazz);
                        }
                    } catch (Exception e) {
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

    /**
     * resolveBase package
     * @return
     */
    private List<String> resolveBasePackage() {
       //try load from properties
        String path = getEnvironment().getProperty("framework.rest.service-path");
        if (StringUtils.isNotEmpty(path)) {
            List<String> paths = Splitter.on(",").trimResults().omitEmptyStrings()
                .splitToList(path);
            return formatPaths(paths);

        }


        //try annotation
        ArrayList<String> packages = Lists.newArrayList();
        Set<Class<?>> configClass = loadConditionClass("com", this::ifValidRestBasePackageScan);
        for (Class<?> aClass : configClass) {
            RestServiceScan restServiceScan = aClass.getAnnotation(RestServiceScan.class);
            if (restServiceScan != null) {
                packages.addAll(Arrays.asList(restServiceScan.serviceBasePackage()));
            }
            EnableRestService  restService = aClass.getAnnotation(EnableRestService.class);
            if (restService != null) {
                packages.addAll(Arrays.asList(restService.serviceBasePackage()));
            }
        }
        log.info("[rest-service] parse base packages: {}",packages);
        return formatPaths(packages);
    }

    /**
     * format paths
     */
    private List<String> formatPaths(List<String> paths) {
        if (!CollectionUtils.isEmpty(paths)) {
            return paths.stream()
                .map(p -> {
                    p = p.replace(".", "/");
                    return String.format(PACKAGE_FORMAT, p);
                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    private boolean ifValidRestServiceBean(Class<?> clazz) {
        String className = clazz.getName();
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

    /**
     * if this class a rest servce package scane
     * @param clazz
     * @return
     */
    private boolean ifValidRestBasePackageScan(Class<?> clazz) {
        return (clazz.getAnnotation(RestServiceScan.class)!=null
            ||clazz.getAnnotation(EnableRestService.class)!=null);
    }

    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(this.getEnvironment().resolveRequiredPlaceholders(basePackage));
    }


    private Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }


}
