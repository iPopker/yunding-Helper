package com.jcc.helper.jcchelper.service.vision;

import com.jcc.helper.jcchelper.domain.Observation;
import org.springframework.web.multipart.MultipartFile;

public interface VisionAdapter {

    Observation analyze(MultipartFile image);
}
