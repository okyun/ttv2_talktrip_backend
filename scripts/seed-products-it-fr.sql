-- MySQL(talktrip): IT/FR + US(10) + MX(10) + AU(20) 시드, 로컬 정적 이미지 (`tt/front_end/public/image`)
-- 이미지 URL은 프론트와 동일 오리진에서 `/image/파일명` (예: http://localhost:5173/image/italy_rome_01.jpg)
-- 실행 예) mysql -u talktrip -ptalktrip123 talktrip < seed-products-it-fr.sql

START TRANSACTION;

SET @now := NOW();

-- Country(IT / FR + US / MX / AU) — 없으면 등록
INSERT INTO `country` (`id`, `continent`, `name`) VALUES
  ('IT', 'Europe', 'Italy'),
  ('FR', 'Europe', 'France'),
  ('US', 'North America', 'United States'),
  ('MX', 'North America', 'Mexico'),
  ('AU', 'Oceania', 'Australia')
ON DUPLICATE KEY UPDATE
  `continent` = VALUES(`continent`),
  `name` = VALUES(`name`);



INSERT IGNORE INTO `member` (
  `member_id`, `account_email`, `phone_num`, `gender`, `birthday`, `name`, `nickname`, `profile_image`,
  `member_role`, `member_state`, `created_at`, `updated_at`
) VALUES (
  4, 'seed-seller-itfr@talktrip.local', NULL, NULL, NULL, 'IT/FR 시드 판매자', 'itfr_seller', NULL,
  'A', 'A', @now, @now
);
SET @seller_id := 4;

-- Italy #1: 로마
SET @thumb_it_1  := '/image/italy_rome_01.jpg';
INSERT INTO `product`
(`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`)
VALUES
('로마 콜로세움 & 바티칸 1일 투어', '로마 핵심 명소(콜로세움, 포로 로마노, 바티칸)를 하루에 효율적으로 즐기는 워킹 투어입니다.', @thumb_it_1, NULL, @seller_id, 'IT', false, NULL, @now, @now);
SET @p_it_1 := LAST_INSERT_ID();

INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '오전 출발(한국어 가이드)', 30, 120000, 99000, @p_it_1),
(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '오후 출발(한국어 가이드)', 30, 120000, 105000, @p_it_1),
(DATE_ADD(CURDATE(), INTERVAL 14 DAY), '프리미엄(소규모 8인)', 12, 180000, 149000, @p_it_1);

INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p_it_1, '/image/italy_rome_01.jpg', 1),
(@p_it_1, '/image/italy_venice_01.jpg', 2),
(@p_it_1, '/image/italy_tuscany_01.jpg', 3);

-- Italy #2: 토스카나/피렌체
SET @thumb_it_2  := '/image/italy_tuscany_01.jpg';
INSERT INTO `product`
(`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`)
VALUES
('피렌체 & 토스카나 와이너리 투어', '피렌체 근교 토스카나의 와이너리에서 테이스팅과 점심을 즐기는 하루 코스입니다.', @thumb_it_2, NULL, @seller_id, 'IT', false, NULL, @now, @now);
SET @p_it_2 := LAST_INSERT_ID();

INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '공용 차량(점심 포함)', 20, 150000, 129000, @p_it_2),
(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '와인 테이스팅 3종', 20, 165000, 139000, @p_it_2),
(DATE_ADD(CURDATE(), INTERVAL 21 DAY), '프라이빗 차량(2~4인)', 6, 320000, 279000, @p_it_2);

INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p_it_2, '/image/italy_tuscany_01.jpg', 1),
(@p_it_2, '/image/italy_rome_01.jpg', 2),
(@p_it_2, '/image/italy_venice_01.jpg', 3);

-- France #1: 루브르(파리)
SET @thumb_fr_1  := '/image/france_museum_01.jpg';
INSERT INTO `product`
(`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`)
VALUES
('파리 루브르 핵심 작품 2시간', '루브르 대표 작품만 골라 짧고 깊게 보는 인문학 도슨트 투어입니다.', @thumb_fr_1, NULL, @seller_id, 'FR', false, NULL, @now, @now);
SET @p_fr_1 := LAST_INSERT_ID();

INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '오전(소규모 10인)', 25, 110000, 95000, @p_fr_1),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '오후(소규모 10인)', 25, 110000, 99000, @p_fr_1),
(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '프리미엄(개인 이어폰 제공)', 20, 130000, 109000, @p_fr_1);

INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p_fr_1, '/image/france_museum_01.jpg', 1),
(@p_fr_1, '/image/france_paris_01.jpg', 2),
(@p_fr_1, '/image/france_mont_01.jpg', 3);

-- France #2: 몽생미셸(노르망디 해안)
SET @thumb_fr_2  := '/image/france_mont_01.jpg';
INSERT INTO `product`
(`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`)
VALUES
('몽생미셸 당일치기 버스 투어', '파리 출발/도착, 몽생미셸 자유시간을 넉넉히 주는 실속 코스입니다.', @thumb_fr_2, NULL, @seller_id, 'FR', false, NULL, @now, @now);
SET @p_fr_2 := LAST_INSERT_ID();

INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '버스(가이드 동행)', 40, 140000, 119000, @p_fr_2),
(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '버스 + 오디오가이드', 40, 150000, 129000, @p_fr_2),
(DATE_ADD(CURDATE(), INTERVAL 20 DAY), '프리미엄(앞좌석 우선)', 20, 170000, 149000, @p_fr_2);

INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p_fr_2, '/image/france_mont_01.jpg', 1),
(@p_fr_2, '/image/france_coast_01.jpg', 2),
(@p_fr_2, '/image/france_paris_01.jpg', 3);

-- ========== United States (10) ==========
-- US #1
SET @thumb := '/image/usa_01.jpg';
INSERT INTO `product`
(`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`)
VALUES
('뉴욕 맨해튼 & 센트럴 파크 풀데이', '타임스퀘어, 센트럴파크, 록펠러(또는 써밋) 등 맨해튼 핵심을 하루에 담는 워킹+지하철(메트로) 투어입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '한국어 가이드(소규모 12인)', 28, 180000, 155000, @p),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '영어 가이드(빅그룹)', 40, 150000, 129000, @p),
(DATE_ADD(CURDATE(), INTERVAL 12 DAY), '프리미엄(프라이빗 2~4인)', 8, 350000, 299000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_01.jpg', 1), (@p, '/image/usa_02.jpg', 2), (@p, '/image/usa_03.jpg', 3);
-- US #2
SET @thumb := '/image/usa_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('로스앤젤레스 할리우드 & 산타모니카', '할리우드 사인, 워크 오브 페임, 그리핑스/그리피스 이후 산타모니카비치까지 “LA 정석” 코스입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '투어 버스(고정 루트)', 32, 160000, 139000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '미니밴(소규모 7인)', 14, 220000, 189000, @p),
(DATE_ADD(CURDATE(), INTERVAL 11 DAY), '야간 일정 추가(썬셋+야경)', 20, 190000, 165000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_02.jpg', 1), (@p, '/image/usa_03.jpg', 2), (@p, '/image/usa_01.jpg', 3);
-- US #3
SET @thumb := '/image/usa_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('애리조나 그랜드캐년(사우스 림) 데이투어', '사우스 림 뷰포인트(모하브 포인트 등)를 연결하며, 붉은 절벽이 펼쳐지는 대표 루트로 안내하는 일정입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '집결버스(일반석)', 36, 240000, 209000, @p),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '핼리콥터 옵션(슬롯 제한)', 8, 520000, 449000, @p),
(DATE_ADD(CURDATE(), INTERVAL 15 DAY), '썬라이즈(새벽) 코스', 20, 280000, 245000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_03.jpg', 1), (@p, '/image/usa_01.jpg', 2), (@p, '/image/usa_02.jpg', 3);
-- US #4
SET @thumb := '/image/usa_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('샌프란시스코 골든게이트 & 피어 39 & 차이나타운', '골든게이트, 피어39(갈매기/즐기기), 케이블카·차이나타운 등 SF 시그니처를 한 판에 담는 코스입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '하프데이(오전/오후)', 30, 150000, 129000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '풀데이(캡처 포인트 확장)', 24, 190000, 165000, @p),
(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '프리미엄(프라이빗)', 6, 360000, 315000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_01.jpg', 1), (@p, '/image/usa_02.jpg', 2), (@p, '/image/usa_03.jpg', 3);
-- US #5
SET @thumb := '/image/usa_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('라스베이거스 스트립 & 프리엄(선택) 야간 투어', '벨라지오, 베네시안, 시티센터 등 야간 네온 풍광 + 프리엄(선택) 루트로 “베이거스”를 한 번에 느낍니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '가이드 동행(대중교통/도보 병행)', 40, 140000, 119000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리엄(공연/쇼 티켓은 별도)', 20, 220000, 189000, @p),
(DATE_ADD(CURDATE(), INTERVAL 7 DAY), '프라이빗(2~3인) 야간', 8, 380000, 335000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_02.jpg', 1), (@p, '/image/usa_03.jpg', 2), (@p, '/image/usa_01.jpg', 3);
-- US #6
SET @thumb := '/image/usa_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('올랜도 테마파크(매직/스튜디오 중 택1) 1Day', '올랜도에서 가장 먹는 하루. 테마를 선택(매직/스튜디오)하고, 동선/대기 꿀팁을 제공하는 1Day 플랜입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '1파크(일반권+팁/동선)', 30, 210000, 185000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '익스프레스(우선) 옵션', 20, 280000, 245000, @p),
(DATE_ADD(CURDATE(), INTERVAL 10 DAY), '1.5~2Day 추천 동선(상담형)', 12, 320000, 279000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_03.jpg', 1), (@p, '/image/usa_01.jpg', 2), (@p, '/image/usa_02.jpg', 3);
-- US #7
SET @thumb := '/image/usa_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('마이애미 사우스비치 & 아트데코(오션드라이브) 워킹', '아트데코 히스토릭, 오션드라이브, 사우스비치 해변까지 “해변+거리+사진”의 정석 루트입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '오전(2~2.5h)', 35, 120000, 105000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '오후(2~2.5h)+선셋', 30, 140000, 119000, @p),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '프리미엄(프라이빗) 촬영 동선', 8, 260000, 225000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_01.jpg', 1), (@p, '/image/usa_02.jpg', 2), (@p, '/image/usa_03.jpg', 3);
-- US #8
SET @thumb := '/image/usa_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('시애틀 스페이스 니들 & 파이크 플레이스 마켓 & 개미시장(간단)', '스페이스 니들에서 뷰를 즐기고, 파이크 플레이스(연어)와 개미시장 등 시애틀 시그니처를 훑는 코스입니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '반나절(시티 중심)', 28, 150000, 129000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '하루(시애틀+유니온흐 주변)', 20, 190000, 165000, @p),
(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '푸드포커스(맛집/마켓 심화)', 16, 210000, 185000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_02.jpg', 1), (@p, '/image/usa_03.jpg', 2), (@p, '/image/usa_01.jpg', 3);
-- US #9
SET @thumb := '/image/usa_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('시카고 딥디쉬 & 밀레니엄·리버워크(반나절 이상)', '딥디쉬(현지 느낌)와 밀레니엄, 리버워크 등 “윈디시티” 랜드마크를 효율적으로 연결합니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '딥디쉬+코어 루트(2.5h)', 26, 130000, 115000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '반일(3~4h) 확장', 22, 160000, 139000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '푸드+야경(선택) 커스텀', 10, 220000, 195000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_03.jpg', 1), (@p, '/image/usa_01.jpg', 2), (@p, '/image/usa_02.jpg', 3);
-- US #10
SET @thumb := '/image/usa_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('워싱턴 D.C. 스미스소니안 & 몰 기념비(반나절/일일)', '내셔널 몰, 링컨, 워싱턴 기념비, 스미스소니안(미술/항공) 등 DC 핵심을 “도보+메트로”로 연결합니다.', @thumb, NULL, @seller_id, 'US', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '핵심(반일)', 30, 140000, 125000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '풀데이(박물관 1~2곳 심화)', 18, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '한국어 가이드(주말 한정, 소규모)', 10, 240000, 215000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/usa_01.jpg', 1), (@p, '/image/usa_02.jpg', 2), (@p, '/image/usa_03.jpg', 3);

-- ========== Mexico (10) ==========
-- MX #1
SET @thumb := '/image/mex_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('멕시코시티 자칼로 & 국궁 & 메트로(구시가지)', '대통령궁(국궁), 대성당, 자칼로, 메트로(현지 느낌)로 CDMX의 중심을 하루에 담는 코스입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '한국어 가이드(소규모)', 22, 160000, 139000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '영어+스페인어(혼성)', 30, 130000, 115000, @p),
(DATE_ADD(CURDATE(), INTERVAL 8 DAY), '프리미엄(프라이빗)', 8, 320000, 275000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_01.jpg', 1), (@p, '/image/mex_02.jpg', 2), (@p, '/image/mex_03.jpg', 3);
-- MX #2
SET @thumb := '/image/mex_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('칸쿤 올인클루시브(리조트·비치) 3N4D 템플릿', '칸쿤에서 리조트·비치를 중심으로 휴식과 액티비티(선착)를 밸런스 맞춘 3박4일 컨셉 투어입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '4성급(올인)', 20, 420000, 365000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '5성급(하이엔드)', 12, 650000, 579000, @p),
(DATE_ADD(CURDATE(), INTERVAL 9 DAY), '패밀리(2룸+키즈)', 8, 720000, 635000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_02.jpg', 1), (@p, '/image/mex_03.jpg', 2), (@p, '/image/mex_01.jpg', 3);
-- MX #3
SET @thumb := '/image/mex_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('치첸이트사 일일(칸쿤/플라야 출발) + 세노트(선택)', '신칸쿤/플라야 출발로 치첸이트사를 집중 탐방하고, 상황에 맞게 세노트를 더하는 1Day입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '그룹(대형버스)', 36, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '소그룹(미니밴)', 16, 260000, 225000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '세노트 포함(시간+비용 별도 안내)', 20, 240000, 215000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_03.jpg', 1), (@p, '/image/mex_01.jpg', 2), (@p, '/image/mex_02.jpg', 3);
-- MX #4
SET @thumb := '/image/mex_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('티후아나 & 로사리토 해변 당일(샌디에이고 연동형)', '티후아나(보더)에서 로사리토 해만까지, 멕시코 느낌의 “짧고 강한” 당일 코스입니다(출입국/비자·여권은 사전에 확인).', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '투어 집결형(그룹)', 28, 150000, 135000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(프라이빗)', 10, 300000, 265000, @p),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '해만 씨푸드(점심 업그레이드)', 18, 180000, 155000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_01.jpg', 1), (@p, '/image/mex_02.jpg', 2), (@p, '/image/mex_03.jpg', 3);
-- MX #5
SET @thumb := '/image/mex_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('플라야 델 카르맨 스노쿨링(보트) + 석회동굴(가능 시)', '카리브 느낌이 살아있는 플라야에서 스노쿨/보트 액티비티를 집중하는 반나절~일정입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '스노쿨(그룹보트)', 32, 190000, 165000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '소규모(투어보트)', 14, 240000, 215000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '석회동/동굴 옵션(날씨 의존)', 20, 220000, 199000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_02.jpg', 1), (@p, '/image/mex_03.jpg', 2), (@p, '/image/mex_01.jpg', 3);
-- MX #6
SET @thumb := '/image/mex_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('오악사카 푸에블로 & 머리알(미토라) 데이(간단 루트)', '오악사카의 스페인식 광장과 시장, ‘머리알’ 개념을 훑는 컴팩트 1Day 루트(동선/일정은 현지 상황에 맞게 조정).', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '푸에블로 중심(반일)', 24, 140000, 125000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '푸에블로+시장(일일)', 20, 170000, 149000, @p),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '쿡킹/미소 수업(한정)', 8, 220000, 199000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_03.jpg', 1), (@p, '/image/mex_01.jpg', 2), (@p, '/image/mex_02.jpg', 3);
-- MX #7
SET @thumb := '/image/mex_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('과달라하라 마리아치 & 테킬라(짐아도어) & 도심', '“마리아치의 본고장” 느낌을 살리며 시장, 짐아도어, 테킬라(시음은 선택)를 연결한 도심 중심 코스입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '이브닝(마리아치 중심)', 26, 150000, 135000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '풀데이(시장+광장+미술/거리)', 20, 180000, 155000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '시음(성인) 프리미엄', 10, 240000, 215000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_01.jpg', 1), (@p, '/image/mex_02.jpg', 2), (@p, '/image/mex_03.jpg', 3);
-- MX #8
SET @thumb := '/image/mex_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('산 미겔 데 아옌데(유네스코) 워킹 & 루프탑(선택)', '콜로니얼 거리, 대성당, 루프탑(선택)으로 “초콜릿/포토” 감성이 강한 도시 투어입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '2~2.5h 핵심', 20, 130000, 115000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '반일(3~4h) 확장', 16, 160000, 145000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '선셋(루프탑) 포함', 12, 200000, 175000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_02.jpg', 1), (@p, '/image/mex_03.jpg', 2), (@p, '/image/mex_01.jpg', 3);
-- MX #9
SET @thumb := '/image/mex_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('누에보 바야르타(멕시코) 선셋+마리나 & 케이블(가능 시)', '마리나 보드워크, 구시가지(부조), 선셋 포인트를 잇는 “해안+거리+야경” 조합 루트입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '해만(오후) 야간 마무리', 22, 150000, 135000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '풀데이(시장+구시가지)', 18, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '쿠집/보트(날씨 의존) 옵션', 8, 280000, 245000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_03.jpg', 1), (@p, '/image/mex_01.jpg', 2), (@p, '/image/mex_02.jpg', 3);
-- MX #10
SET @thumb := '/image/mex_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('툴룸 유적 + 세노트 스노쿨링(반나절~일일)', '카리브 해안 툴룸에서 유적+바다(또는 세노트)의 조합이 자연스럽게 이어지는 1Day입니다.', @thumb, NULL, @seller_id, 'MX', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '유적 중심(반일)', 24, 170000, 155000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '세노트/스노쿨 포함(일정 확장)', 20, 220000, 199000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(프라이빗)', 6, 420000, 365000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/mex_01.jpg', 1), (@p, '/image/mex_02.jpg', 2), (@p, '/image/mex_03.jpg', 3);

-- ========== Australia (20) ==========
-- AU #1
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('시드니 하버 & 오페라 하우스(외관) + 록스(간단) 워킹', '서큘러 키, 오페라(외부), 록스 일대를 “사진+거리+바람”이 좋은 하버 워크로 이어갑니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '2~3h 풋워크(소규모)', 30, 130000, 115000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '반일(쿠잘스 연동, 선택)', 20, 170000, 155000, @p),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '프리미엄(프라이빗)', 8, 300000, 265000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #2
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('멜번 그레이트 오션 로드(12사도 일대) 데이투어', '대표 전망대와 해안절벽, 소도시(러너) 등 GOR의 “인스타+드라이브” 정석 루트(장거리·일정에 따라 셔틀/버스).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '집결버스(대형) 그룹', 36, 220000, 195000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '소그룹(12인 내)', 16, 280000, 245000, @p),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '힐스/레이크 부분(일정 강도 조정)', 10, 300000, 265000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #3
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('케언즈 GBR(아우터) 스노쿨링(보트) + 뷔페(가능 시)', '그레이트배리어리프의 “진짜 색”을 느끼는 아우터/오프쇼어 스노쿨 중심 1Day(기상 영향).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '그룹(대형) 보트', 40, 280000, 245000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '스몰(소형) 전용(인원 제한)', 14, 360000, 315000, @p),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '2다이빙/액티비(선택) 업그레이드', 8, 420000, 375000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #4
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('브리즈번 쿠잘스 & 루웨이 강(도심) 워크', '쿠잘스의 상점/식당과 루웨이 강, 스토리 브릿지 일대의 “따뜻한” 도심 루트입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '반일(2.5h)', 28, 120000, 105000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '푸드(브런치) 포커스', 16, 150000, 135000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '야간(강/야경) 확장', 20, 140000, 125000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #5
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('퍼스 프리멘털 & 킹스 파크(도심+전망) 워크', '스완강, 프리멘털, 킹스파크 뷰포인트로 “서호주의 여유+전망”을 짧고 확실히 담는 코스입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '2~3h 핵심', 30, 130000, 115000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '반일(카페+거리+전망)', 22, 160000, 145000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '선셋(시간/장소) 확장', 12, 190000, 169000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #6
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('애들레이드 바로사 밸리 와인(셔틀) 데이투어', '셔릉턴/쇼크 등 와이너리 2~3곳(티스팅) + 점심(선택)으로 구성되는 와인 데이(음주/운전 주의).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '그룹(셔틀) 와인 2~3', 32, 240000, 215000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '하이엔드(2곳+식사)', 10, 320000, 285000, @p),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '스파/숙(연동) 상담형', 6, 420000, 375000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #7
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('캔버라 국회(외관) & 전쟁기념관(간단) 시티투어', '호주의 수도(계획도시) 감성을 “국가 상징+전망+정돈”으로 느끼는 하프데이~일정입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '4h 코어', 24, 150000, 135000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '6h 확장(박물관/전시)', 16, 180000, 165000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(프라이빗)', 6, 300000, 265000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #8
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('골드코스트 해변 & 쿨랑가타(간단) 워크', '쿨랑가타~서퍼스 파라다이스 일대의 “파도+보드워크+카페” 느낌의 해변 워크입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '2~2.5h(오전)', 32, 120000, 105000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '선셋(오후) 확장', 24, 140000, 125000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '서핑(입문) 액티비', 10, 200000, 175000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #9
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('태즈매니아 호바트 & 살라맨더 마켓(반일)', '이 작은 수도(호주 남끝)의 품격—마켓, 항구, 케이블(선택)을 연결하는 도심 루트입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '마켓(아침) + 시티', 20, 140000, 125000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '푸드집중(씨푸드) 확장', 12, 180000, 165000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '마운트웰링턴(날씨 의존) 당일', 8, 220000, 199000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #10
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('NT 다윈(일몰/야경) 수변 쿨러 투어', '뜨거운 토피컬·수변 워터프론트, 일몰(날씨/계절) 느낌이 좋을 때 추천하는 “느긋” 코스입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '수변(이브닝)', 22, 120000, 105000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '야시장(가능한 날) 연동', 16, 140000, 125000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '2일(카쿠두/옵션) 상담형', 6, 400000, 355000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #11
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('케언즈 쿠란다 레인포레스트(스카이레일+마켓)', '쿠란다 마켓과 레인포레스트(스카이레일/기차 조합)를 중심으로 “녹음+뷰”를 즐기는 1Day입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '기본(레일+셔틀) 패키지', 28, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '동물원/빌리지(가능) 확장', 20, 230000, 205000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(프라이빗 운전)', 8, 360000, 315000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #12
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('퍼스 로트네스트(쿼카) 페리(당일) 투어', '페리로 Rottnest, 쿼카(야생) 외에 맑은 해변/라이딩(선택)로 하루를 꽉 채웁니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '페리(고정) + 섬 루프(자유)', 32, 240000, 215000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '자전거(대여) 포함(체력)', 20, 270000, 245000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '스노쿨(날씨/접지) 옵션', 10, 300000, 265000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #13
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('NT 울루루(일출/해질) & 카타츄쿠(필드 오브라이트 가이드)', '레드센터의 절벽, 일출/해질(날씨), 주변 워크로 “호주의 심장” 느낌을 풀스크린으로(장거리·숙은 별도).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '3~4h 코어(일출/일몰 1곳)', 16, 320000, 285000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '2회(새벽+저녁) 프리미엄', 10, 450000, 405000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '필드 오브 라이트(시즌/상황)', 8, 360000, 325000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #14
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('빅토리아 필립아일랜드 펭귄 패럿(이브닝)', '일몰 펭귄 패럿은 “빅토리아 시그니처” 야행성 체험(사진/소음/규정 준수) 중심입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '투어 집결(기본) 관람', 24, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '퍼스트뷰(한정) 업그레이드', 8, 280000, 245000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '이브닝(도심 연동) 확장', 12, 240000, 215000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #15
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('NSW 블루마운틴(3시스터즈) 데이(시드니 집결)', '스카이웨이/엑토리드레일(상황)로 절벽+숲+전망을 훑는 1Day(이동 거리 多).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '집결버스(일반) 그룹', 32, 240000, 215000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '12인 스몰 그룹', 14, 300000, 265000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '초보 하이크(짧은 코스) 포함', 10, 280000, 255000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #16
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('퀸즈랜드 Daintree(세계유산) + 케이프트리(간단) 데이', '세계에서 가장 오래된 열대우림 지대 인근(일정/날씨)으로 “초열대+리버” 느낌을 느끼는 투어입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '핵심(레인포+리버) 중심', 20, 260000, 235000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '크로커(보트/쿠로즈) 옵션(날씨)', 12, 320000, 285000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '소그룹(장거리) 프리미엄', 6, 420000, 375000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #17
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('퍼스 스완 밸리 와이너리(쉬라/카베르네) 데이(셔틀)', '스완 밸리의 셰드·티스팅, 점심(선택)로 구성하는 와인 데이(과음/운전 주의).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '와인 2~3(그룹 셔틀)', 28, 250000, 225000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '3~4(프리미엄 식사) 확장', 10, 320000, 285000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '퍼스 왕복(맞춤) 프라이빗', 4, 520000, 465000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);
-- AU #18
SET @thumb := '/image/aus_03.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('SA 캥거루 섬(킹스콧/페넌쇼) 데이(페리) 투어', '페리로 “야생+조용한” 섬을, 킹스콧·쇼(상황) 중심으로 1Day를 꾸리는 루트(일정/페리 영향).', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '핵심(페리+섬 루프) 그룹', 22, 220000, 199000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '쇼(상황) 강화', 12, 240000, 215000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(프라이빗 운전/페리 조합) 상담', 4, 480000, 425000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_03.jpg', 1), (@p, '/image/aus_01.jpg', 2), (@p, '/image/aus_02.jpg', 3);
-- AU #19
SET @thumb := '/image/aus_01.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('케언즈(GBR) 잠수함/반잠수(기상) + 스노쿨(조합) 데이', 'GBR 뷰잉을 “잠수함/스노쿨” 둘 다로 보강(날씨/슬롯)하는 하루짜리 조합 루트입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '스노쿨 중심(잠수함 슬롯은 대기/교체)', 30, 300000, 265000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '잠수함(우선) + 스노쿨', 18, 340000, 305000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '프리미엄(소인원 보트) 업그레이드', 8, 450000, 405000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_01.jpg', 1), (@p, '/image/aus_02.jpg', 2), (@p, '/image/aus_03.jpg', 3);
-- AU #20
SET @thumb := '/image/aus_02.jpg';
INSERT INTO `product` (`product_name`, `description`, `thumbnail_image_url`, `thumbnail_image_hash`, `seller_id`, `country_id`, `deleted`, `deleted_at`, `created_at`, `updated_at`) VALUES
('퀸즈랜드 골드코스트(무버/드림/씨월드 중 택1) 1Day', '거대 테마존(선택)에서 대기/동선/식사 꿀팁을 섞는 “1파크 1Day” 투어 템플릿입니다.', @thumb, NULL, @seller_id, 'AU', false, NULL, @now, @now);
SET @p := LAST_INSERT_ID();
INSERT INTO `product_option` (`start_date`, `option_name`, `stock`, `price`, `discount_price`, `product_id`) VALUES
(CURDATE(), '1파크(기본) 동선+팁', 28, 200000, 175000, @p),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '익스프레스(우선) 옵션', 16, 250000, 225000, @p),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '2Day 추천(스테이+교통) 상담', 6, 320000, 285000, @p);
INSERT INTO `product_image` (`product_id`, `image_url`, `sort_order`) VALUES
(@p, '/image/aus_02.jpg', 1), (@p, '/image/aus_03.jpg', 2), (@p, '/image/aus_01.jpg', 3);

COMMIT;
