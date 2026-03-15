package com.jcc.helper.jcchelper.service.rag;

import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.StateDiff;
import org.springframework.stereotype.Component;

@Component
public class QueryPlanner {

    public String plan(GameState state, StateDiff diff) {
        String hpTrend = diff.hpDelta() == null ? "unknown" : (diff.hpDelta() < 0 ? "dropping" : "stable");
        return "stage=" + state.stage()
                + ", level=" + state.level()
                + ", gold=" + state.gold()
                + ", hpTrend=" + hpTrend
                + ", ask=transition_and_action";
    }
}
