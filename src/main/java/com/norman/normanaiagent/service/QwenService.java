package com.norman.normanaiagent.service;

import com.norman.normanaiagent.domain.request.QwenMultiModalRequest;
import com.norman.normanaiagent.domain.request.QwenTextRequest;
import com.norman.normanaiagent.domain.response.QwenResponse;
import io.reactivex.Flowable;

public interface QwenService {

    QwenResponse chat(QwenTextRequest request);

    Flowable<QwenResponse> streamChat(QwenTextRequest request);

    QwenResponse multiModalChat(QwenMultiModalRequest request);

    Flowable<QwenResponse> streamMultiModalChat(QwenMultiModalRequest request);
}
