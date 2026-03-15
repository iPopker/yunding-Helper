package com.jcc.helper.jcchelper.api;

import com.jcc.helper.jcchelper.api.dto.StateDiffResponse;
import com.jcc.helper.jcchelper.domain.StateDiff;
import com.jcc.helper.jcchelper.persistence.StateDiffRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/games")
public class GameTraceController {

    private final StateDiffRepository stateDiffRepository;

    public GameTraceController(StateDiffRepository stateDiffRepository) {
        this.stateDiffRepository = stateDiffRepository;
    }

    @GetMapping("/{gameId}/diffs")
    public List<StateDiffResponse> getDiffs(@PathVariable String gameId) {
        return stateDiffRepository.findByGameId(gameId).stream()
                .map(this::toResponse)
                .toList();
    }

    private StateDiffResponse toResponse(StateDiff diff) {
        return new StateDiffResponse(
                diff.fromTurn(),
                diff.toTurn(),
                diff.goldDelta(),
                diff.hpDelta(),
                diff.levelDelta(),
                diff.shopChanged(),
                diff.boardChanged(),
                diff.benchChanged(),
                diff.itemChanged(),
                diff.summary()
        );
    }
}
