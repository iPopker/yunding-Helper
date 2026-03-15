package com.jcc.helper.jcchelper.service;

import com.jcc.helper.jcchelper.api.dto.AnalyzeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AnalyzeService {

    public AnalyzeResponse analyze(String gameId, MultipartFile image) {
        return new AnalyzeResponse(
                gameId,
                1,
                "当前血量安全，经济可控，建议优先稳步提升战力。",
                "前排过渡 + 后排输出体系",
                List.of(
                        "优先补足前排质量，保留核心对子",
                        "本回合不强行刷新，保留利息到关键节点"
                ),
                List.of(
                        "当前战力与经济处于可控区间",
                        "商店信息不足以支持激进转型"
                ),
                List.of(
                        "若下一回合对手强度提升，可能出现连续掉血"
                ),
                List.of(
                        "当前为 mock 识别结果，未接入真实视觉服务"
                ),
                0.70
        );
    }
}
