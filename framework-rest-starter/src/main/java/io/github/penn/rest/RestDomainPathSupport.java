package io.github.penn.rest;

import com.google.common.base.Splitter;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Spliterator;

/**
 * @author tangzhongping
 */
public abstract class RestDomainPathSupport {

    @Autowired
    private RestServiceDomainProperties restServiceDomainProperties;

    private String domainBaseUrl;

    /**
     * get domain key
     * @return
     */
    public abstract String getDomainKey();

    @PostConstruct
    public void init(){
        domainBaseUrl=restServiceDomainProperties.findDomain(getDomainKey());
        if (StringUtils.isEmpty(domainBaseUrl)) {
            throw new IllegalStateException("no domain key=" + getDomainKey() + " was find.");
        }
    }


    /**
     * add segment
     */
    public String addPath(String path){
        HttpUrl.Builder builder = HttpUrl.parse(domainBaseUrl).newBuilder();
        List<String> paths = Splitter.on("/").omitEmptyStrings()
                .trimResults().splitToList(path);
        paths.forEach(builder::addPathSegment);
        return builder.toString();
    }

}
