package org.example.schema;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.*;

public class VarcharField {
    private static final MilvusClientV2 client;
    static {
        client = new MilvusClientV2(ConnectConfig.builder()
                .uri("http://localhost:19530")
                .build());
    }

    private static CreateCollectionReq.CollectionSchema createSchema() {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        schema.setEnableDynamicField(true);

        schema.addField(AddFieldReq.builder()
                .fieldName("varchar_field1")
                .dataType(DataType.VarChar)
                .maxLength(100)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("varchar_field2")
                .dataType(DataType.VarChar)
                .maxLength(200)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("pk")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .build());

        schema.addField(AddFieldReq.builder()
                .fieldName("embedding")
                .dataType(DataType.FloatVector)
                .dimension(3)
                .build());

        return schema;
    }

    private static List<IndexParam> createIndex() {
        List<IndexParam> indexes = new ArrayList<>();
        indexes.add(IndexParam.builder()
                .fieldName("varchar_field1")
                .indexName("varchar_index")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .build());

        indexes.add(IndexParam.builder()
                .fieldName("embedding")
                .indexType(IndexParam.IndexType.AUTOINDEX)
                .metricType(IndexParam.MetricType.COSINE)
                .build());

        return indexes;
    }

    private static void createCollection(CreateCollectionReq.CollectionSchema schema, List<IndexParam> indexes) {
        CreateCollectionReq requestCreate = CreateCollectionReq.builder()
                .collectionName("my_varchar_collection")
                .collectionSchema(schema)
                .indexParams(indexes)
                .build();
        client.createCollection(requestCreate);
    }

    private static void insert() {
        List<JsonObject> rows = new ArrayList<>();
        Gson gson = new Gson();
        rows.add(gson.fromJson("{\"varchar_field1\": \"Product A\", \"varchar_field2\": \"High quality product\", \"pk\": 1, \"embedding\": [0.1, 0.2, 0.3]}", JsonObject.class));
        rows.add(gson.fromJson("{\"varchar_field1\": \"Product B\", \"varchar_field2\": \"Affordable price\", \"pk\": 2, \"embedding\": [0.4, 0.5, 0.6]}", JsonObject.class));
        rows.add(gson.fromJson("{\"varchar_field1\": \"Product C\", \"varchar_field2\": \"Best seller\", \"pk\": 3, \"embedding\": [0.7, 0.8, 0.9]}", JsonObject.class));

        InsertResp insertR = client.insert(InsertReq.builder()
                .collectionName("my_varchar_collection")
                .data(rows)
                .build());
    }

    private static void query() {
        String filter = "varchar_field1 == \"Product A\"";
        QueryResp resp = client.query(QueryReq.builder()
                .collectionName("my_varchar_collection")
                .filter(filter)
                .outputFields(Arrays.asList("varchar_field1", "varchar_field2"))
                .build());

        System.out.println(resp.getQueryResults());
    }

    private static void search() {
        String filter = "varchar_field1 == \"Product A\"";
        SearchResp resp = client.search(SearchReq.builder()
                .collectionName("my_varchar_collection")
                .annsField("embedding")
                .data(Collections.singletonList(new FloatVec(new float[]{0.3f, -0.6f, 0.1f})))
                .topK(5)
                .outputFields(Arrays.asList("varchar_field1", "varchar_field2"))
                .filter(filter)
                .build());

        System.out.println(resp.getSearchResults());
    }

    public static void main(String[] args) {
        CreateCollectionReq.CollectionSchema schema = createSchema();
        List<IndexParam> indexes = createIndex();
        createCollection(schema, indexes);
        insert();
        query();
        search();
    }
}
