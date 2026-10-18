package com.xhzb.nursing.tool;

import com.xhzb.nursing.domain.vo.NursingProjectVo;
import com.xhzb.nursing.service.INursingProjectService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NursingProjectTool {

    @Autowired
    private INursingProjectService nursingProjectService;

    @Tool(name = "find_project_tool",description = "查找护理项目")
    public List<NursingProjectVo> findAllProjectVo(){
        return nursingProjectService.listAll();
    }
}
