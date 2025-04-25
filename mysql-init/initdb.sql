SET FOREIGN_KEY_CHECKS = 0;

-- 1. merchant_service 테이블 먼저 생성
CREATE TABLE IF NOT EXISTS merchant_service
(
    merchantid VARCHAR(50) PRIMARY KEY COMMENT '상점 고유 ID',
    mcpyn      ENUM ('Y','N') DEFAULT 'N' COMMENT 'MCP 여부(Y/N)'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='상점 서비스 속성 관리';

-- 2. van 테이블 생성
CREATE TABLE IF NOT EXISTS van
(
    vanno INT AUTO_INCREMENT PRIMARY KEY COMMENT 'VAN사 번호',
    payto VARCHAR(50) COMMENT '결제처 또는 정산 주체'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='VAN사 정보 관리';

-- 3. service_currency (merchant_service, van 둘 다 참조)
CREATE TABLE IF NOT EXISTS service_currency
(
    merchantid    VARCHAR(50) NOT NULL COMMENT '상점 고유 ID',
    cardcode      VARCHAR(20) NOT NULL COMMENT '결제 카드 코드',
    reqcurrency   CHAR(3)     NOT NULL COMMENT '요청 통화 ISO 코드',
    apprvcurrency CHAR(3)     NOT NULL COMMENT '승인 통화 ISO 코드',
    payto         DECIMAL(15, 2) DEFAULT 0 COMMENT '정산 지급 대상금액',
    minamount     DECIMAL(15, 2) DEFAULT 0 COMMENT '최소 결제 금액',
    vanno         INT         NOT NULL COMMENT 'VAN사 번호',
    PRIMARY KEY (merchantid, cardcode, reqcurrency),
    INDEX idx_currency (apprvcurrency),
    INDEX idx_service_currency_vanno (vanno),
    FOREIGN KEY (vanno) REFERENCES van (vanno),
    FOREIGN KEY (merchantid) REFERENCES merchant_service (merchantid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='상점별 지원 통화 및 조건 설정';

-- 4. exchange_rate 테이블 생성
CREATE TABLE IF NOT EXISTS exchange_rate
(
    exgno      VARCHAR(20) PRIMARY KEY COMMENT '환율 고유 번호',
    frcurrency CHAR(3)        NOT NULL COMMENT '기준 통화 ISO 코드',
    tocurrency CHAR(3)        NOT NULL COMMENT '대상 통화 ISO 코드',
    rate       DECIMAL(12, 6) NOT NULL DEFAULT 0 COMMENT '적용 환율 정보'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='환율 관리';

CREATE INDEX idx_exchange_rate_currency ON exchange_rate (frcurrency, tocurrency);

-- 5. kr_cardbin 테이블 생성 (독립 테이블)
CREATE TABLE IF NOT EXISTS kr_cardbin
(
    bin VARCHAR(20) PRIMARY KEY COMMENT '카드 BIN 번호'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='카드 BIN 관리';

-- 6. transactions 테이블 생성 (merchant_service 참조)
CREATE TABLE IF NOT EXISTS transactions
(
    trans_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '결제 거래 고유 번호',
    merchantid     VARCHAR(50) NOT NULL COMMENT '상점 고유 ID',
    trans_datetime DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '결제 요청 일시',
    trans_status   VARCHAR(20) NOT NULL COMMENT '거래 상태',
    reqcurrency    CHAR(3) COMMENT '요청된 통화',
    apprvcurrency  CHAR(3) COMMENT '승인된 통화',
    cardcode       VARCHAR(20) COMMENT '카드 결제 코드',
    amount         DECIMAL(15, 2)       DEFAULT 0 COMMENT '거래 금액',
    result_code    VARCHAR(10) COMMENT '결과 코드',
    result_message VARCHAR(255) COMMENT '결과 메시지',
    INDEX idx_merchantid (merchantid),
    INDEX idx_trans_datetime (trans_datetime),
    FOREIGN KEY (merchantid) REFERENCES merchant_service (merchantid)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='상점 결제 거래 이력';

CREATE TABLE IF NOT EXISTS payment
(
    payment_id  VARCHAR(36) PRIMARY KEY COMMENT '결제 고유 ID (UUID)',
    merchant_id VARCHAR(64) NOT NULL COMMENT '상점 ID',
    card_number VARCHAR(64) NOT NULL COMMENT '카드번호',
    amount      BIGINT      NOT NULL COMMENT '결제금액',
    status      VARCHAR(20) NOT NULL COMMENT '결제상태'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='결제 정보';

INSERT INTO payment (payment_id, merchant_id, card_number, amount, status)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'M123', '4111111111111111', 10000, 'APPROVED'),
       ('223e4567-e89b-12d3-a456-426614174001', 'M124', '5111111111111111', 20000, 'APPROVED'),
       ('323e4567-e89b-12d3-a456-426614174002', 'M125', '4111111111111112', 30000, 'PENDING'),
       ('423e4567-e89b-12d3-a456-426614174003', 'M126', '5111111111111113', 15000, 'APPROVED'),
       ('523e4567-e89b-12d3-a456-426614174004', 'M127', '4111111111111114', 25000, 'FAILED'),
       ('623e4567-e89b-12d3-a456-426614174005', 'M128', '5111111111111115', 35000, 'APPROVED'),
       ('723e4567-e89b-12d3-a456-426614174006', 'M129', '4111111111111116', 45000, 'APPROVED'),
       ('823e4567-e89b-12d3-a456-426614174007', 'M130', '5111111111111117', 55000, 'PENDING'),
       ('923e4567-e89b-12d3-a456-426614174008', 'M131', '4111111111111118', 65000, 'APPROVED'),
       ('a23e4567-e89b-12d3-a456-426614174009', 'M132', '5111111111111119', 75000, 'FAILED'),
       ('b23e4567-e89b-12d3-a456-426614174010', 'M133', '4111111111111120', 85000, 'APPROVED');

SET FOREIGN_KEY_CHECKS = 1;