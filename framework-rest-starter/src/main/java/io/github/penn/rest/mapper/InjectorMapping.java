package io.github.penn.rest.mapper;

import lombok.Getter;

/**
 * Injector Mapping
 */
public class InjectorMapping {

    @Getter
    private String sourcePath;
    @Getter
    private String targetPath;

    private InjectorMapping(){}

    private InjectorMapping(String sourcePath,String targetPath){
        this.sourcePath=sourcePath;
        this.targetPath=targetPath;
    }

    public static InjectorMapping DEFAULT_MAPPING =new InjectorMapping("","");


    public static InjectorMapping mapping(String sourcePath,String targetPath){
        return new InjectorMapping(sourcePath,targetPath);
    }


}
