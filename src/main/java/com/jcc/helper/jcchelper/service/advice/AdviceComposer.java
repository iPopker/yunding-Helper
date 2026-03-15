package com.jcc.helper.jcchelper.service.advice;

import com.jcc.helper.jcchelper.domain.GameMemory;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.RetrievalChunk;
import com.jcc.helper.jcchelper.domain.StateDiff;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdviceComposer {

    public StructuredAdvice compose(GameState state,
                                    StateDiff diff,
                                    GameMemory memory,
                                    List<RetrievalChunk> chunks) {
        String compDirection = state.level() <= 5
                ? "前排过渡 + 后排稳定输出"
                : "中期提质量，准备向核心阵容过渡";

        List<String> actions = buildActions(state, diff);
        List<String> reasons = new ArrayList<>();
        reasons.add(diff.summary());
        reasons.add(memory.continuityNotes());
        if (!chunks.isEmpty()) {
            reasons.add("检索证据: " + chunks.get(0).text());
        }

        List<String> uncertainties = new ArrayList<>();
        uncertainties.add("当前为 M2 阶段，视觉识别仍为 mock。");
        uncertainties.add(chunks.isEmpty() ? "未命中外部知识，使用本地 fallback 知识。" : "检索结果已参与建议生成。");

        double confidence = Math.max(0.0, Math.min(1.0, state.stateConfidence()));
        return new StructuredAdvice(
                memory.summary(),
                compDirection,
                actions,
                reasons,
                memory.keyRisks(),
                uncertainties,
                confidence
        );
    }

    private List<String> buildActions(GameState state, StateDiff diff) {
        if (diff.hpDelta() != null && diff.hpDelta() < 0) {
            return List.of(
                    "优先补足当前回合战力，避免继续掉血。",
                    "减少无效刷新，保留可转型对子。"
            );
        }
        if (state.gold() >= 30) {
            return List.of(
                    "优先保利息，下一节点集中提质量。",
                    "围绕现有装备确定主 C 过渡线。"
            );
        }
        return List.of(
                "本回合控制开销，做即时战力替换。",
                "若下回合仍弱势，再加速搜牌。"
        );
    }
}
