package com.jcc.helper.jcchelper.service.state;

import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.Observation;
import com.jcc.helper.jcchelper.domain.StateDiff;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StateEngine {

    public GameState buildGameState(String gameId, int turnIndex, Observation observation) {
        return new GameState(
                gameId,
                turnIndex,
                observation.stage(),
                observation.gold(),
                observation.level(),
                observation.hp(),
                observation.shopUnits(),
                observation.items(),
                observation.boardUnits(),
                observation.benchUnits(),
                observation.augments(),
                observation.confidence(),
                observation.confidence()
        );
    }

    public StateDiff calculateDiff(GameState previous, GameState current) {
        if (previous == null) {
            return new StateDiff(
                    current.gameId(),
                    null,
                    current.turnIndex(),
                    null,
                    null,
                    null,
                    true,
                    true,
                    true,
                    true,
                    "首回合状态建立，暂无上一回合可比较。"
            );
        }

        int goldDelta = current.gold() - previous.gold();
        int hpDelta = current.hp() - previous.hp();
        int levelDelta = current.level() - previous.level();
        boolean shopChanged = !Objects.equals(previous.shopUnits(), current.shopUnits());
        boolean boardChanged = !Objects.equals(previous.boardUnits(), current.boardUnits());
        boolean benchChanged = !Objects.equals(previous.benchUnits(), current.benchUnits());
        boolean itemChanged = !Objects.equals(previous.items(), current.items());

        String summary = String.format(
                "经济变化 %d，血量变化 %d，等级变化 %d，商店%s，棋盘%s。",
                goldDelta,
                hpDelta,
                levelDelta,
                shopChanged ? "有变化" : "无变化",
                boardChanged ? "有变化" : "无变化"
        );

        return new StateDiff(
                current.gameId(),
                previous.turnIndex(),
                current.turnIndex(),
                goldDelta,
                hpDelta,
                levelDelta,
                shopChanged,
                boardChanged,
                benchChanged,
                itemChanged,
                summary
        );
    }
}
