package io.github.penn.rest.mapper;

import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.logging.log4j.util.Strings;

/**
 * Injector Mapping
 */
public class InjectorMapping {

  private static final String ROOT = "ROOT";
  /**
   * path maps
   */
  public static class PathMapping {


    //source 2 target
    Map<String, String> pathMappings = Maps.newHashMap();

    private PathMapping() {
    }

    public PathMapping addMapping(String sourcePath, String targetPath) {
      pathMappings.put(sourcePath, targetPath);
      return this;
    }

    public PathMapping sourceRootMapping(String sourcePath){return addMapping(sourcePath,ROOT);}


    public PathMapping targetRootMapping(String targetPath) {
      return addMapping(ROOT, targetPath);
    }

    public PathMapping addDefaultMapping() {
      return addMapping(ROOT, ROOT);
    }




    public Map<String, String> readingMappings() {
      return pathMappings;
    }


  }


  public static final PathMapping DEFAULT_MAPPING = new PathMapping().addDefaultMapping();

  public static PathMapping newPathMappings() {
    return new PathMapping();
  }

  public static  boolean isRootPath(String path) {
    return ROOT.equalsIgnoreCase(path);
  }
}
