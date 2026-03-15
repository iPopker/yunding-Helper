package com.jcc.helper.jcchelper.service;

import com.jcc.helper.jcchelper.api.dto.AnalyzeResponse;
import com.jcc.helper.jcchelper.domain.GameMemory;
import com.jcc.helper.jcchelper.domain.GameState;
import com.jcc.helper.jcchelper.domain.Observation;
import com.jcc.helper.jcchelper.domain.StateDiff;
import com.jcc.helper.jcchelper.persistence.AdviceTraceRepository;
import com.jcc.helper.jcchelper.persistence.GameSessionRepository;
import com.jcc.helper.jcchelper.persistence.MemorySummaryRepository;
import com.jcc.helper.jcchelper.persistence.StateDiffRepository;
import com.jcc.helper.jcchelper.persistence.TurnStateRepository;
import com.jcc.helper.jcchelper.service.state.MemoryEngine;
import com.jcc.helper.jcchelper.service.state.StateEngine;
import com.jcc.helper.jcchelper.service.vision.VisionAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class AnalyzeService {

    private final VisionAdapter visionAdapter;
    private final GameSessionRepository gameSessionRepository;
    private final TurnStateRepository turnStateRepository;
    private final StateEngine stateEngine;
    private final StateDiffRepository stateDiffRepository;
    private final MemorySummaryRepository memorySummaryRepository;
    private final MemoryEngine memoryEngine;
    private final AdviceTraceRepository adviceTraceRepository;

    public AnalyzeService(VisionAdapter visionAdapter,
                          GameSessionRepository gameSessionRepository,
                          TurnStateRepository turnStateRepository,
                          StateEngine stateEngine,
                          StateDiffRepository stateDiffRepository,
                          MemorySummaryRepository memorySummaryRepository,
                          MemoryEngine memoryEngine,
                          AdviceTraceRepository adviceTraceRepository) {
        this.visionAdapter = visionAdapter;
        this.gameSessionRepository = gameSessionRepository;
        this.turnStateRepository = turnStateRepository;
        this.stateEngine = stateEngine;
        this.stateDiffRepository = stateDiffRepository;
        this.memorySummaryRepository = memorySummaryRepository;
        this.memoryEngine = memoryEngine;
        this.adviceTraceRepository = adviceTraceRepository;
    }

    @Transactional
    public AnalyzeResponse analyze(String gameId, MultipartFile image) {
        int turnIndex = gameSessionRepository.nextTurnIndex(gameId);
        Optional<GameState> previousState = turnStateRepository.findLatestByGameId(gameId);
        Optional<GameMemory> previousMemory = memorySummaryRepository.findLatestByGameId(gameId);

        Observation observation = visionAdapter.analyze(image);
        GameState currentState = stateEngine.buildGameState(gameId, turnIndex, observation);
        StateDiff diff = stateEngine.calculateDiff(previousState.orElse(null), currentState);
        GameMemory memory = memoryEngine.buildMemory(previousMemory.orElse(null), currentState, diff);

        turnStateRepository.save(currentState);
        stateDiffRepository.save(diff);
        memorySummaryRepository.save(memory);

        AnalyzeResponse response = new AnalyzeResponse(
                gameId,
                turnIndex,
                memory.summary(),
                chooseCompDirection(currentState),
                buildActions(currentState, diff),
                List.of(diff.summary(), memory.continuityNotes()),
                memory.keyRisks(),
                List.of("当前为 M1 mock 视觉识别，后续接入真实 OCR/模板识别。"),
                currentState.stateConfidence()
        );
        adviceTraceRepository.save(gameId, turnIndex, response);
        return response;
    }

    private String chooseCompDirection(GameState state) {
        if (state.level() <= 5) {
            return "前排过渡 + 后排稳定输出";
        }
        return "中期提质量，准备向核心阵容过渡";
    }

    private List<String> buildActions(GameState state, StateDiff diff) {
        if (diff.hpDelta() != null && diff.hpDelta() < 0) {
            return List.of(
                    "优先补足当前回合战力，避免继续连败掉血",
                    "保留关键对子和可转型组件，避免无效刷新"
            );
        }
        if (state.gold() >= 30) {
            return List.of(
                    "保持利息并观察下一阶段商店质量",
                    "围绕现有装备确定一条主 C 过渡线"
            );
        }
        return List.of(
                "本回合控制开销，优先做性价比最高的上场替换",
                "若下回合战力仍不足，再考虑加速搜牌"
        );
    }
}
