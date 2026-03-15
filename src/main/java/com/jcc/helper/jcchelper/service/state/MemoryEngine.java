package com.jcc.helper.jcchelper.service.state;

import com.jcc.helper.jcchelper.domain.GameMemory;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.StateDiff;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemoryEngine {

    public GameMemory buildMemory(GameMemory previousMemory, GameState currentState, StateDiff diff) {
        List<String> risks = new ArrayList<>();
        if (currentState.hp() <= 40) {
            risks.add("当前血量较低，需优先稳血。");
        }
        if (diff.hpDelta() != null && diff.hpDelta() < 0) {
            risks.add("上一回合发生掉血，可能存在战力缺口。");
        }
        if (currentState.gold() < 20) {
            risks.add("经济偏低，后续容错能力受限。");
        }
        if (risks.isEmpty()) {
            risks.add("暂无高优先级风险。");
        }

        String continuityPrefix = previousMemory == null
                ? "新对局起点。"
                : "承接上回合记忆：" + previousMemory.summary();
        String continuityNotes = continuityPrefix + " 当前阶段 " + currentState.stage() + "。";
        String summary = "第 " + currentState.turnIndex() + " 回合："
                + "金币 " + currentState.gold()
                + "，血量 " + currentState.hp()
                + "，等级 " + currentState.level() + "。";

        return new GameMemory(
                currentState.gameId(),
                currentState.turnIndex(),
                summary,
                risks,
                continuityNotes
        );
    }
}
