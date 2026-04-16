package com.talktrip.talktrip.global.exception;

import lombok.Getter;

@Getter
public class ReviewException extends CustomException {
    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }
}


