package com.example.demo.entity;

import io.github.penn.rest.mapper.Joint;
import lombok.Data;

@Data
public class JointClass {

    @Joint(using = "$.status",domain = "penn")
    private String id;
    @Joint(using = "$.msg",domain = "a")
    private String nodeId;

}
