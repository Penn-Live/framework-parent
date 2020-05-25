package io.github.penn.rest.mapper;


import lombok.Data;

import java.util.List;

@Data
public class JointClass extends JointBase {

    @Joint(using = "$.data.a")
    private int a;
    @Joint(using = "$.data.b")
    private Integer b;
    @Joint(using = "$.data.c")
    private int c;
    @Joint(using = "$.data.d",domain = "penn")
    private JointInner d;
    @Joint(using = "$.data.e")
    private List<String> e;

    @Data
    static class JointInner extends JointBase{
        @Joint(using = "$.ac")
        private String a;
    }



}
