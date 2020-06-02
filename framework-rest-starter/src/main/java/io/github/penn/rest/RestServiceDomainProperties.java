package io.github.penn.rest;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author tangzhongping
 */
@ConfigurationProperties("rest")
@Data
public class RestServiceDomainProperties {

    private Map<String, String> domain;


    /**
     * find domain
     */
    public String findDomain(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        if (CollectionUtils.isEmpty(domain)) {
            return null;
        }
        return domain.get(key);
    }


    public boolean isEmpty() {
        return domain == null || domain.isEmpty();
    }

}
