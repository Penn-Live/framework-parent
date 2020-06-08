package io.github.penn.rest.mapper;

/**
 * Injector Mapping
 */
public class InjectorMapping {

    private String sourcePath;
    private String targetPath;

    private InjectorMapping(){}

    private InjectorMapping mapping(String targetPath,String sourcePath){
        this.sourcePath=sourcePath;
        this.targetPath=targetPath;
        return this;
    }


}
