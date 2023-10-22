package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    MAX_ACCOUNT_COUNT("생성 가능한 최대 계좌 입니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNT_UNMATCHED("사용자와 계좌의 소유주가 다릅니다."),
    ACCOUNT_ALREADY_DELETED("이미 해지된 계좌입니다."),
    ACCOUNT_DELETE_HAS_BALANCE("잔액이 있는 계좌는 해지할 수 없습니다."),
    NOT_ENOUGH_BALANCE("잔액이 부족합니다."),
    AMOUNT_EXCEED_BALANCE("거래 금액은 잔액보다 클 수 없습니다."),
    TRANSACTION_NOT_FOUND("거래 내역을 찾을 수 없습니다."),
    TRANSACTION_UNMATCHED("접근할 수 없는 거래 내역입니다."),
    CANCEL_MUST_FULLY("부분 취소는 허용되지 않습니다."),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래는 취소할 수 없습니다."),
    INVALIDED_REQUEST("잘못된 요청입니다."),
    ACCOUNT_TRANSACTION_LOCK_FAILED("해당 계좌는 사용중입니다.");


    private final String description;
}
