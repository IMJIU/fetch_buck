
create sequence start 1;
-- ----------------------------
-- Table structure for buck_trend
-- ----------------------------
DROP TABLE IF EXISTS "ott"."buck_trend";
CREATE TABLE "ott"."buck_trend" (
"id" int4,
"dt" varchar(12) COLLATE "default",
"ord" int4,
"code" varchar(10) COLLATE "default",
"trade_name" varchar(20) COLLATE "default",
"up_per" float8,
"core_m" float8,
"core_p" float8,
"huge_m" float8,
"huge_p" float8,
"big_m" float8,
"big_p" float8,
"mid_m" float8,
"mid_p" float8,
"small_m" float8,
"small_p" float8,
"bname" varchar(20) COLLATE "default",
"bcode" varchar(20) COLLATE "default",
"sh" float8,
"sz" float8
)
WITH (OIDS=FALSE)

;
-- ----------------------------
-- Table structure for plate
-- ----------------------------
DROP TABLE IF EXISTS "ott"."plate";
CREATE TABLE "ott"."plate" (
"id" int4,
"dt" varchar(12) COLLATE "default",
"code" varchar(20) COLLATE "default",
"val" float8,
"deal_m" float8,
"increment_m" float8,
"increment_p" float8,
"ext" varchar(100) COLLATE "default"
)
WITH (OIDS=FALSE)

;