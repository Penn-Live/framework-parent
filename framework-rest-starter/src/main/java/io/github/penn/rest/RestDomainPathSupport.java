package io.github.penn.rest;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author tangzhongping
 */
@Slf4j
public abstract class RestDomainPathSupport {

    @Autowired
    private RestServiceProperties restServiceDomainProperties;

    private String domainBaseUrl;

    public RestDomainPathSupport() { }

    @PostConstruct
    public void init(){
        domainBaseUrl=restServiceDomainProperties.findDomain(StringUtils.defaultString(getDomainKey()));

        if (restServiceDomainProperties.isEmpty()) {
            log.warn("empty domain properties, please check.");
            return;
        }
        if (StringUtils.isEmpty(domainBaseUrl)) {
            throw new IllegalStateException("no domain key=" + getDomainKey() + " was find.");
        }
    }

    /**
     * 获取domain的key
     * @return
     */
    public abstract String getDomainKey();



    /**
     * add segment
     */
    public String addPath(String path){
        if (StringUtils.isAnyEmpty(this.domainBaseUrl,path)) {
            log.error("path or base domain url can't be null, please check");
            return null;
        }
        HttpUrl.Builder builder = HttpUrl.parse(this.domainBaseUrl).newBuilder();
        List<String> paths = Splitter.on("/").omitEmptyStrings()
                .trimResults().splitToList(path);
        paths.forEach(builder::addPathSegment);
        return builder.toString();
    }

}
