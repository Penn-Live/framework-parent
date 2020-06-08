package io.github.penn.rest.mapper;

/**
 * @author tangzhongping
 */
public class JointBase {


    /**
     * joint
     */
    public<T> JointBase joint(T source,String domain){
        JointUtil.joint(this, source, domain);
        return this;
    }

    public<T> JointBase using(T source,String domain){
        JointUtil.joint(this, source, domain);
        return this;
    }

}
