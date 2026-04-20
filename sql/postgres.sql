CREATE TABLE "public"."vector_store"
(
    "id"        uuid NOT NULL,
    "content"   text COLLATE "pg_catalog"."default",
    "metadata"  jsonb,
    "embedding" "public"."vector",
    CONSTRAINT "vector_store_pkey" PRIMARY KEY ("id")
)
;

ALTER TABLE "public"."vector_store"
    OWNER TO "postgres";

CREATE INDEX "spring_ai_vector_index" ON "public"."vector_store" (
                                                                  "embedding" "public"."vector_cosine_ops" ASC NULLS LAST
    );