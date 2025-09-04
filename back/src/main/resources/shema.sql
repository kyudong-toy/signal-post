-- 테스트틀 위해 PostgreSQL의 'jsonb' 타입을 H2의 'json' 타입으로 사용하도록 별칭을 만듭니다.
CREATE TYPE "jsonb" AS json;