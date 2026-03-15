package com.jcc.helper.jcchelper.service.vision;

import com.jcc.helper.jcchelper.domain.Observation;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class MockVisionAdapter implements VisionAdapter {

    @Override
    public Observation analyze(MultipartFile image) {
        String fileName = image.getOriginalFilename() == null ? "unknown" : image.getOriginalFilename();
        int signal = image.isEmpty() ? 0 : Math.abs(fileName.hashCode() % 10);
        return new Observation(
                "2-" + ((signal % 3) + 1),
                20 + signal,
                5 + (signal % 2),
                90 - signal,
                List.of("unit_A", "unit_B", "unit_" + signal),
                List.of("item_sword", "item_armor"),
                List.of("board_tank", "board_carry"),
                List.of("bench_1", "bench_2"),
                List.of("augment_placeholder"),
                0.65 + (signal * 0.01)
        );
    }
}
